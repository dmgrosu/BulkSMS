package com.emotion.ecm.service;

import com.emotion.ecm.dao.SmsMessageDao;
import com.emotion.ecm.enums.MessageStatus;
import com.emotion.ecm.enums.PreviewStatus;
import com.emotion.ecm.model.*;
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

    public List<SubmitSmDto> getAllToBeSendByPreview(List<SmsPreview> previews, SmscAccount smscAccount) {

        List<SubmitSmDto> result = new ArrayList<>();

        for (SmsPreview preview : previews) {
            List<SmsMessage> messages = getMessagesToSendByPreview(preview, preview.getTps());
            for (SmsMessage message : messages) {
                result.add(convertMessageToSubmitSmDto(preview, message, smscAccount));
            }

        }

        return result;
    }

    public List<SmsMessage> batchSave(List<SmsMessage> messages) {
        return smsMessageDao.saveAll(messages);
    }

    public Set<String> getSentDestinationsByPreviewId(long previewId) {
        return smsMessageDao.getSentDestinationsByPreviewId(previewId);
    }

    private SubmitSmDto convertMessageToSubmitSmDto(SmsPreview preview, SmsMessage message, SmscAccount smscAccount) {

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


    private List<SmsMessage> getMessagesToSendByPreview(SmsPreview preview, int maxSize) {

        List<SmsMessage> result = new ArrayList<>();

        List<String> numbersList = getDestinations(preview);

        if (numbersList != null && !numbersList.isEmpty()) {

            List<SmsPrefix> prefixes = smsPrefixService.getAllPrefixesByAccount(preview.getUser().getAccount());

            List<SmsText> smsParts = createSmsParts(preview.getText());

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

        }

        if (result.size() > 0) {
            preview.setPreviewStatus(PreviewStatus.APPROVED);
            preview.setTotalParts(result.size());
            smsPreviewService.saveAndFlush(preview);
        }

        return result;
    }

    private List<String> getDestinations(SmsPreview preview) {

        List<String> result = destinationsMap.get(preview.getId());

        final Set<String> sentDestinations = new HashSet<>();

        if (result == null) {
            if (preview.getSentParts() > 0) {
                sentDestinations.addAll(getSentDestinationsByPreviewId(preview.getId()));
            }
            result = new ArrayList<>();
        } else {
            if (preview.isTextEdited()) {
                sentDestinations.addAll(getSentDestinationsByPreviewId(preview.getId()));
                destinationsMap.remove(preview.getId());
                result = new ArrayList<>();
            } else {
                return result;
            }
        }

        AccountData accountData = preview.getAccountData();
        Set<Group> groups = preview.getGroups();
        String phoneNumbers = preview.getPhoneNumbers();
        if (accountData != null) {
            result = getNumbersFromFile(accountData, preview).stream()
                    .filter(s -> !sentDestinations.contains(s))
                    .collect(Collectors.toList());
        } else if (groups != null && !groups.isEmpty()) {
            result = getNumbersFromGroup(groups).stream()
                    .filter(s -> !sentDestinations.contains(s))
                    .collect(Collectors.toList());
        } else if (phoneNumbers != null && !phoneNumbers.isEmpty()) {
            result = getNumbersFromString(phoneNumbers).stream()
                    .filter(s -> !sentDestinations.contains(s))
                    .collect(Collectors.toList());
        }

        destinationsMap.put(preview.getId(), result);

        return result;
    }

    private List<SmsText> createSmsParts(String initialText) {

        List<SmsText> result = new ArrayList<>();

        List<String> smsParts = StringUtil.createSmsParts(initialText, false);
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
                    .collect(Collectors.toList());
        }

        return result;
    }

    private List<String> getNumbersFromGroup(Set<Group> groups) {
        return contactService.getAllPhoneNumbersByGroups(groups);
    }

    private List<String> getNumbersFromFile(AccountData accountData, SmsPreview preview) {

        List<String> result = new ArrayList<>();

        try {
            Path directory = accountDataService.getAccountPath(preview.getUser());
            Path fullPath = directory.resolve(accountData.getFileName());
            BufferedReader reader = Files.newBufferedReader(fullPath);
            String currLine;
            while ((currLine = reader.readLine()) != null) {
                result.add(currLine);
            }
            reader.close();
        } catch (IOException e) {
            LOGGER.error(e.getMessage());
            return result;
        }

        return result;
    }

}
