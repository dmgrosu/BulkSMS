package com.emotion.ecm.service;

import com.emotion.ecm.dao.SmsMessageDao;
import com.emotion.ecm.enums.MessageStatus;
import com.emotion.ecm.enums.PreviewStatus;
import com.emotion.ecm.exception.PreviewException;
import com.emotion.ecm.model.*;
import com.emotion.ecm.model.dto.PreviewDto;
import com.emotion.ecm.model.dto.SubmitSmDto;
import com.emotion.ecm.util.StringUtil;
import org.jsmpp.bean.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class SmsMessageService {

    private static final Logger LOGGER = LoggerFactory.getLogger(SmsMessageService.class);

    private SmsMessageDao smsMessageDao;
    private SmsPrefixService smsPrefixService;
    private SmsPreviewService smsPreviewService;
    private SmsTextService smsTextService;
    private ContactService contactService;
    private AccountDataService accountDataService;

    private final ConcurrentHashMap<Long, List<String>> destinationsMap = new ConcurrentHashMap<>();

    @Autowired
    public SmsMessageService(SmsMessageDao smsMessageDao, SmsPrefixService smsPrefixService,
                             SmsPreviewService smsPreviewService, SmsTextService smsTextService,
                             ContactService contactService, AccountDataService accountDataService) {
        this.smsMessageDao = smsMessageDao;
        this.smsPrefixService = smsPrefixService;
        this.smsPreviewService = smsPreviewService;
        this.smsTextService = smsTextService;
        this.contactService = contactService;
        this.accountDataService = accountDataService;
    }

    public List<SubmitSmDto> getAllToBeSendByPreview(List<PreviewDto> previews, SmscAccount smscAccount) {
        return previews.stream()
                .map(previewDto -> getMessagesToSendByPreview(previewDto, previewDto.getTps()))
                .flatMap(Collection::stream)
                .map(message -> convertMessageToSubmitSmDto(message, smscAccount))
                .collect(Collectors.toList());
    }

    public List<SmsMessage> batchSave(List<SmsMessage> messages) {
        return smsMessageDao.saveAll(messages);
    }

    public Set<String> getSentDestinationsByPreviewId(long previewId) {
        return smsMessageDao.getSentDestinationsByPreviewId(previewId);
    }

    private SubmitSmDto convertMessageToSubmitSmDto(SmsMessage message, SmscAccount smscAccount) {

        SmsPreview preview = message.getPreview();
        SmppAddress smppAddress = preview.getSmppAddress();

        SubmitSmDto result = new SubmitSmDto();

        result.setServiceType("");
        result.setDestinationTon(TypeOfNumber.ALPHANUMERIC);
        result.setDestinationNpi(NumberingPlanIndicator.UNKNOWN);
        result.setDestinationNumber(message.getDestAddress());
        result.setSourceTon(smppAddress.getTon());
        result.setSourceNpi(smppAddress.getNpi());
        result.setSourceNumber(smppAddress.getAddress());
        result.setEsmClass(new ESMClass());
        result.setProtocolId((byte) 0);
        result.setPriorityFlag((byte) 0);
        result.setScheduleDeliveryTime("");
        result.setValidityPeriod(preview.getExpirationTime().getValue());
        SMSCDeliveryReceipt deliveryReceipt = SMSCDeliveryReceipt.DEFAULT;
        if (preview.isDlr()) {
            deliveryReceipt = SMSCDeliveryReceipt.SUCCESS_FAILURE;
        }
        result.setRegisteredDelivery(new RegisteredDelivery(deliveryReceipt));
        result.setReplaceIfPresentFlag((byte) 0);
        result.setDataCoding(new GeneralDataCoding());
        result.setSmDefaultMsgId((byte) 0);
        result.setShortMessage(message.getSmsText().getText().getBytes());

        return result;
    }


    private List<SmsMessage> getMessagesToSendByPreview(PreviewDto previewDto, int maxSize) {

        List<SmsMessage> result = new ArrayList<>();

        if (previewDto == null) {
            LOGGER.error("previewDto is null");
            return result;
        }

        List<String> numbersList = getDestinations(previewDto);

        if (numbersList != null && !numbersList.isEmpty()) {

            List<SmsPrefix> prefixes = smsPrefixService.getAllPrefixesByUserId(previewDto.getUserId());

            List<SmsText> smsParts = createSmsParts(previewDto.getText());

            SmsPreview preview;
            try {
                preview = smsPreviewService.getPreviewById(previewDto.getPreviewId());
            } catch (PreviewException e) {
                LOGGER.error(e.getMessage());
                return result;
            }

            Iterator<String> iterator = numbersList.iterator();
            SmsMessage message;
            while (iterator.hasNext()) {

                String destAddr = iterator.next();

                SmsPrefix prefix = getPrefixForMsisdn(prefixes, destAddr);
                boolean stop = false;
                for (int i = 0; i < smsParts.size(); i++) {
                    SmsText smsPart = smsParts.get(i);
                    if (result.size() >= maxSize && i == smsParts.size() - 1) {
                        stop = true;
                        break;
                    }
                    message = new SmsMessage();
                    message.setDestAddress(destAddr);
                    message.setMessageStatus(MessageStatus.READY);
                    message.setPreview(preview);
                    message.setSmsText(smsPart);
                    message.setSmsPrefix(prefix);
                    result.add(message);
                }
                if (stop) {
                    break;
                }

                iterator.remove();
            }
            if (result.size() > 0) {
                preview.setPreviewStatus(PreviewStatus.APPROVED);
                preview.setRecipientsCount(previewDto.getRecipientsCount());
                preview.setTotalParts(previewDto.getRecipientsCount() * smsParts.size());
                smsPreviewService.saveAndFlush(preview);
            }
        }

        return result;
    }

    private List<String> getDestinations(PreviewDto previewDto) {

        List<String> result = destinationsMap.get(previewDto.getPreviewId());

        final Set<String> sentDestinations = new HashSet<>();

        if (result == null) {
            if (previewDto.getTotalSent() > 0) {
                sentDestinations.addAll(getSentDestinationsByPreviewId(previewDto.getPreviewId()));
            }
            result = new ArrayList<>();
        } else {
            if (previewDto.isTextEdited()) {
                sentDestinations.addAll(getSentDestinationsByPreviewId(previewDto.getPreviewId()));
                destinationsMap.remove(previewDto.getPreviewId());
                result = new ArrayList<>();
            } else {
                return result;
            }
        }

        int accountDataId = previewDto.getAccountDataId();
        Set<Integer> groupIds = previewDto.getGroupIds();
        String phoneNumbers = previewDto.getPhoneNumbers();
        if (accountDataId != 0) {
            result = getNumbersFromFile(previewDto.getAccountDataId()).stream()
                    .filter(s -> !sentDestinations.contains(s))
                    .collect(Collectors.toList());
        } else if (groupIds != null && !groupIds.isEmpty()) {
            result = getNumbersFromGroup(groupIds).stream()
                    .filter(s -> !sentDestinations.contains(s))
                    .collect(Collectors.toList());
        } else if (phoneNumbers != null && !phoneNumbers.isEmpty()) {
            result = getNumbersFromString(phoneNumbers).stream()
                    .filter(s -> !sentDestinations.contains(s))
                    .collect(Collectors.toList());
        }

        int totalNumbers = result.size();

        if (totalNumbers > 0) {
            destinationsMap.put(previewDto.getPreviewId(), result);
            previewDto.setRecipientsCount(totalNumbers);
        }

        return result;
    }

    private List<SmsText> createSmsParts(String initialText) {

        List<SmsText> result = new ArrayList<>();

        List<String> smsParts = StringUtil.createSmsParts(initialText);
        short partNumber = 1;
        for (String smsPart : smsParts) {
            result.add(smsTextService.getByTextAndParts(smsPart, partNumber, (short) smsParts.size()));
            partNumber++;
        }

        return result;
    }

    private SmsPrefix getPrefixForMsisdn(List<SmsPrefix> prefixes, String destAddr) {
        for (SmsPrefix prefix : prefixes) {
            if (destAddr.startsWith(prefix.getPrefix())) {
                return prefix;
            }
        }
        return null;
    }

    private List<String> getNumbersFromString(String phoneNumbers) {

        List<String> result = new ArrayList<>();

        if (phoneNumbers != null && !phoneNumbers.isEmpty()) {
            String[] arr = phoneNumbers.split(",");
            result = Arrays.stream(arr)
                    .filter(s -> !s.isEmpty())
                    .collect(Collectors.toList());
        }

        return result;
    }

    private List<String> getNumbersFromGroup(Set<Integer> groupIds) {
        return contactService.getAllPhoneNumbersByGroups(groupIds);
    }

    private List<String> getNumbersFromFile(int accountDataId) {
        List<String> result = new ArrayList<>();
        try {
            result = accountDataService.getNumbersFromFile(accountDataId);
        } catch (IOException e) {
            LOGGER.error(e.getMessage());
        }
        return result;
    }

}
