package com.emotion.ecm.service;

import com.emotion.ecm.dao.AppUserDao;
import com.emotion.ecm.dao.SmsMessageDao;
import com.emotion.ecm.enums.MessageStatus;
import com.emotion.ecm.exception.ExpirationTimeException;
import com.emotion.ecm.exception.PreviewException;
import com.emotion.ecm.exception.SmppAddressException;
import com.emotion.ecm.model.*;
import com.emotion.ecm.model.dto.*;
import com.emotion.ecm.util.StringUtil;
import org.jsmpp.bean.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.LocalDateTime;
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
    private SmppAddressService smppAddressService;
    private ExpirationTimeService expirationTimeService;
    private BlackListService blackListService;
    private AppUserDao userDao;

    private final ConcurrentHashMap<Long, List<SmsMessageDto>> destinationsMap = new ConcurrentHashMap<>();

    @Autowired
    public SmsMessageService(SmsMessageDao smsMessageDao, SmsPrefixService smsPrefixService,
                             SmsPreviewService smsPreviewService, SmsTextService smsTextService,
                             ContactService contactService, AccountDataService accountDataService,
                             SmppAddressService smppAddressService, ExpirationTimeService expirationTimeService,
                             BlackListService blackListService, AppUserDao userDao) {
        this.smsMessageDao = smsMessageDao;
        this.smsPrefixService = smsPrefixService;
        this.smsPreviewService = smsPreviewService;
        this.smsTextService = smsTextService;
        this.contactService = contactService;
        this.accountDataService = accountDataService;
        this.smppAddressService = smppAddressService;
        this.expirationTimeService = expirationTimeService;
        this.blackListService = blackListService;
        this.userDao = userDao;
    }

    @Transactional
    public List<SmsMessage> batchSave(List<SmsMessage> messages) {
        return smsMessageDao.saveAll(messages);
    }

    @Transactional
    public void updateMessagesToSentFromMessagePack(SubmitSmDto[] messagePack) {
        if (messagePack == null) {
            LOGGER.error("message pack is null");
            return;
        }
        Arrays.stream(messagePack)
                .filter(dto -> dto.getId() > 0)
                .filter(dto -> dto.getMessageId() != null && !dto.getMessageId().isEmpty())
                .forEach(dto -> smsMessageDao.updateMessageIdById(dto.getId(), dto.getMessageId(),
                        dto.getSubmitRespTime(), MessageStatus.SENT));
    }

    @Transactional
    public void updateMessageStatusById(long id, MessageStatus status) {
        smsMessageDao.updateMessageStatusById(id, status);
    }

    @Transactional
    public void updateDlrDateById(long messageDbId, MessageStatus newStatus) {
        smsMessageDao.updateDlrDateById(messageDbId, newStatus, LocalDateTime.now());
    }

    SubmitSmDto[] createMessagePackFromPreviewList(Map<PreviewDto, Integer> previews) {

        List<SubmitSmDto> result = new ArrayList<>();

        if (previews == null || previews.isEmpty()) {
            LOGGER.error("preview map is null or empty");
            return new SubmitSmDto[0];
        }

        for (Map.Entry<PreviewDto, Integer> previewEntry : previews.entrySet()) {
            PreviewDto preview = previewEntry.getKey();
            try {
                SmppAddressDto smppAddress = smppAddressService.getDtoById(preview.getSmppAddressId());
                String expTimeValue = expirationTimeService.getExpirationTimeValueById(preview.getExpirationTimeId());

                List<SmsMessage> messages = getMessagesToSendByPreview(preview, preview.getTps());

                if (messages.size() > 0) {
                    List<SmsMessage> savedMessages = batchSave(messages);
                    if (savedMessages.size() > 0) {
                        smsPreviewService.updatePreviewSentCountById(preview.getPreviewId(), messages.size());
                        result.addAll(messages.stream()
                                .filter(message -> message.getMessageStatus() == MessageStatus.READY)
                                .map((message) ->
                                        convertMessageToSubmitSmDto(message, smppAddress,
                                                expTimeValue, preview.isDlr()))
                                .collect(Collectors.toList()));
                    }
                } else {
                    smsPreviewService.updatePreviewToCompletedById(preview.getPreviewId());
                    destinationsMap.remove(preview.getPreviewId());
                }
            } catch (SmppAddressException | ExpirationTimeException ex) {
                LOGGER.error(ex.getMessage());
            }
        }

        return result.toArray(new SubmitSmDto[0]);
    }

    private Set<String> getSentDestinationsByPreviewId(long previewId) {
        return smsMessageDao.getSentDestinationsByPreviewId(previewId);
    }

    private SubmitSmDto convertMessageToSubmitSmDto(SmsMessage message, SmppAddressDto smppAddress,
                                                    String expTimeValue, boolean dlr) {

        SubmitSmDto result = new SubmitSmDto();

        if (message == null || smppAddress == null) {
            return result;
        }

        result.setId(message.getId());
        result.setServiceType("");
        result.setDestinationTon(TypeOfNumber.ALPHANUMERIC);
        result.setDestinationNpi(NumberingPlanIndicator.UNKNOWN);
        result.setDestinationNumber(message.getDestAddress());
        result.setSourceTon(smppAddress.getTon());
        result.setSourceNpi(smppAddress.getNpi());
        result.setSourceNumber(smppAddress.getAddress());
        result.setEsmClass(new ESMClass());
        result.setProtocolId((byte) 0); // to set sms type here
        result.setPriorityFlag((byte) 0);
        result.setScheduleDeliveryTime("");
        result.setValidityPeriod(expTimeValue);
        SMSCDeliveryReceipt deliveryReceipt = SMSCDeliveryReceipt.DEFAULT;
        if (dlr) {
            deliveryReceipt = SMSCDeliveryReceipt.SUCCESS_FAILURE;
        }
        result.setRegisteredDelivery(new RegisteredDelivery(deliveryReceipt));
        result.setReplaceIfPresentFlag((byte) 0);
        result.setDataCoding(new GeneralDataCoding());
        result.setSmDefaultMsgId((byte) 0);
        result.setShortMessage(message.getSmsText().getText().getBytes());
        result.setOptionalParameters(new OptionalParameter[0]);

        return result;
    }

    private List<SmsMessage> getMessagesToSendByPreview(PreviewDto previewDto, int maxSize) {

        List<SmsMessage> result = new ArrayList<>();

        if (previewDto == null) {
            LOGGER.error("previewDto is null");
            return result;
        }

        List<SmsMessageDto> messageDtoList = getDestinations(previewDto);

        if (messageDtoList == null) {
            LOGGER.warn("message dto list is null");
            return result;
        }
        if (messageDtoList.size() == 0) {
            return result;
        }

        List<SmsPrefix> prefixes = smsPrefixService.getAllPrefixesByUserId(previewDto.getUserId());

        List<SmsText> smsParts = createSmsParts(previewDto.getText());
        if (smsParts == null || smsParts.size() == 0) {
            LOGGER.warn("sms parts list is null or empty");
            return result;
        }

        SmsPreview preview;
        try {
            preview = smsPreviewService.getPreviewById(previewDto.getPreviewId());
        } catch (PreviewException e) {
            LOGGER.error(e.getMessage());
            return result;
        }

        Iterator<SmsMessageDto> iterator = messageDtoList.iterator();
        SmsMessage message;
        while (iterator.hasNext()) {

            SmsMessageDto smsMessageDto = iterator.next();

            SmsPrefix prefix = getPrefixForMsisdn(prefixes, smsMessageDto.getDestAddress());
            boolean stop = false;
            for (int i = 0; i < smsParts.size(); i++) {
                SmsText smsPart = smsParts.get(i);
                if (result.size() >= maxSize && i == smsParts.size() - 1) {
                    stop = true;
                    break;
                }
                message = new SmsMessage();
                message.setDestAddress(smsMessageDto.getDestAddress());
                message.setMessageStatus(smsMessageDto.getMessageStatus());
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

        return result;
    }

    private List<SmsMessageDto> getDestinations(PreviewDto previewDto) {

        List<SmsMessageDto> result = destinationsMap.get(previewDto.getPreviewId());

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

        Set<String> blackList = getBlackList(previewDto.getUserId());

        Integer accountDataId = previewDto.getAccountDataId();
        Set<Integer> groupIds = previewDto.getGroupIds();
        String phoneNumbers = previewDto.getPhoneNumbers();
        if (accountDataId != null) {
            result = getNumbersFromFile(previewDto.getAccountDataId()).stream()
                    .filter(s -> !sentDestinations.contains(s))
                    .map(destAddr -> createMessageDto(destAddr, blackList))
                    .collect(Collectors.toList());
        } else if (groupIds != null && !groupIds.isEmpty()) {
            result = getNumbersFromGroup(groupIds).stream()
                    .filter(s -> !sentDestinations.contains(s))
                    .map(destAddr -> createMessageDto(destAddr, blackList))
                    .collect(Collectors.toList());
        } else if (phoneNumbers != null && !phoneNumbers.isEmpty()) {
            result = getNumbersFromString(phoneNumbers).stream()
                    .filter(s -> !sentDestinations.contains(s))
                    .map(destAddr -> createMessageDto(destAddr, blackList))
                    .collect(Collectors.toList());
        }

        int totalNumbers = result.size();

        if (totalNumbers > 0) {
            destinationsMap.put(previewDto.getPreviewId(), result);
            previewDto.setRecipientsCount(totalNumbers);
        }

        return result;
    }

    private SmsMessageDto createMessageDto(String destAddr, Set<String> blackList) {
        SmsMessageDto result = new SmsMessageDto();
        result.setDestAddress(destAddr);
        result.setMessageStatus(MessageStatus.READY);
        if (blackList != null && blackList.contains(destAddr)) {
            result.setMessageStatus(MessageStatus.BLACKLISTED);
        }
        return result;
    }

    private Set<String> getBlackList(int userId) {
        try {
            Optional<AppUser> optional = userDao.findById(userId);
            if (optional.isPresent()) {
                Integer accountId = optional.get().getAccount().getId();
                return blackListService.getBlacklistedMsisdn(accountId);
            }
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
        }
        return new HashSet<>();
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
                    .filter(s -> !s.trim().isEmpty())
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
