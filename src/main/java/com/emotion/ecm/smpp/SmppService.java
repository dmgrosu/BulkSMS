package com.emotion.ecm.smpp;

import com.emotion.ecm.enums.MessageStatus;
import com.emotion.ecm.exception.SendingException;
import com.emotion.ecm.model.dto.DeliveryDto;
import com.emotion.ecm.model.dto.SubmitSmDto;
import com.emotion.ecm.service.EsmeService;
import com.emotion.ecm.service.SmsMessageService;
import com.emotion.ecm.service.SmscAccountService;
import com.emotion.ecm.util.StringUtil;
import org.jsmpp.bean.RegisteredDelivery;
import org.jsmpp.bean.SMSCDeliveryReceipt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.ZoneId;
import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class SmppService {

    private static final Logger LOGGER = LoggerFactory.getLogger(SmppService.class);

    private final ConcurrentHashMap<Integer, SmppConnection> smppConnections = new ConcurrentHashMap<>();
    private SmscAccountService smscAccountService;
    private EsmeService esmeService;
    private SmsMessageService smsMessageService;
    private final ConcurrentHashMap<String, DeliveryDto> dlrWaitingList = new ConcurrentHashMap<>();

    @Autowired
    public SmppService(SmscAccountService smscAccountService, EsmeService esmeService,
                       SmsMessageService smsMessageService) {
        this.smscAccountService = smscAccountService;
        this.esmeService = esmeService;
        this.smsMessageService = smsMessageService;
    }

    @Scheduled(fixedRate = 1000)
    public void sendMessages() {
        if (smppConnections.isEmpty()) {
            updateSmppConnections();
        }
        if (smppConnections.isEmpty()) {
            LOGGER.warn("No SMPP connections available!");
            return;
        }
        smppConnections.keySet().forEach(smscId -> {
            SubmitSmDto[] messagePack = esmeService.getMessagePackFromQueue(smscId);
            if (messagePack != null) {
                try {
                    sendMessagePack(smppConnections.get(smscId), messagePack);
                } catch (SendingException e) {
                    LOGGER.warn(e.getMessage());
                    esmeService.returnMessagePackInQueue(smscId, messagePack);
                }
            }
        });
    }

    @Async
    @Scheduled(fixedRate = 30000)
    public void clearExpiredMessages() {
        long currentTimeInMillis = System.currentTimeMillis();
        for (String messageId : dlrWaitingList.keySet()) {
            DeliveryDto deliveryDto = dlrWaitingList.get(messageId);
            if (currentTimeInMillis > deliveryDto.getMaxTimeInMillis()) {
                smsMessageService.updateMessageStatusById(deliveryDto.getMessageDbId(), MessageStatus.EXPIRED);
                dlrWaitingList.remove(messageId);
            }
        }
    }

    @Async
    public void processDeliverySm(String messageId, String messageStatus) {
        DeliveryDto deliveryDto = dlrWaitingList.remove(messageId);
        if (deliveryDto == null) {
            LOGGER.warn(String.format("[%s] no delivery dto in deliveryWaitList", messageId));
            return;
        }
        MessageStatus newStatus = MessageStatus.REJECTED;
        switch (messageStatus) {
            case "DELIVRD":
                newStatus = MessageStatus.DELIVERED;
                break;
            case "EXPIRED":
                newStatus = MessageStatus.EXPIRED;
                break;
            default:
                break;
        }
        smsMessageService.updateDlrDateById(deliveryDto.getMessageDbId(), newStatus);
    }

    private void sendMessagePack(SmppConnection smppConnection, SubmitSmDto[] messagePack) throws SendingException {
        smppConnection.sendBulkMessages(messagePack);
        smsMessageService.updateMessagesToSentFromMessagePack(messagePack);
        addToDeliveryWaitingList(messagePack);
    }

    private void updateSmppConnections() {
        smscAccountService.getAllDto()
                .forEach(smsc -> smppConnections.put(smsc.getSmscAccountId(), new SmppConnection(smsc, this)));
    }

    private void addToDeliveryWaitingList(SubmitSmDto[] messagePack) {
        RegisteredDelivery defaultRegDlr = new RegisteredDelivery(SMSCDeliveryReceipt.DEFAULT);
        Arrays.stream(messagePack).parallel()
                .filter(dto -> dto.getRegisteredDelivery() != defaultRegDlr)
                .forEach(dto -> {
                    DeliveryDto deliveryDto = new DeliveryDto(dto.getId(), dto.getMessageId());
                    long submDateInMillis = dto.getSubmitRespTime().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
                    deliveryDto.setMaxTimeInMillis(submDateInMillis + StringUtil.convertExpirationTimeInMillis(dto.getValidityPeriod()));
                    dlrWaitingList.put(dto.getMessageId(), deliveryDto);
                });
    }

}
