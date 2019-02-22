package com.emotion.ecm.smpp;

import com.emotion.ecm.exception.SendingException;
import com.emotion.ecm.model.dto.SubmitSmDto;
import com.emotion.ecm.service.EsmeService;
import com.emotion.ecm.service.SmsMessageService;
import com.emotion.ecm.service.SmscAccountService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;

@Service
public class SmppService {

    private static final Logger LOGGER = LoggerFactory.getLogger(SmppService.class);

    private final ConcurrentHashMap<Integer, SmppConnection> smppConnections;
    private SmscAccountService smscAccountService;
    private EsmeService esmeService;
    private SmsMessageService smsMessageService;

    @Autowired
    public SmppService(SmscAccountService smscAccountService, EsmeService esmeService,
                       SmsMessageService smsMessageService) {
        this.smscAccountService = smscAccountService;
        this.smppConnections = new ConcurrentHashMap<>();
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
                sendMessagePack(smppConnections.get(smscId), messagePack);
            }
        });
    }

    private void sendMessagePack(SmppConnection smppConnection, SubmitSmDto[] messagePack) {
        if (messagePack == null) {
            return;
        }
        try {
            smppConnection.sendBulkMessages(messagePack);
            smsMessageService.updateMessagesToSentFromMessagePack(messagePack);
        } catch (SendingException e) {
            LOGGER.warn("Can't send messages: ", e);
        }
    }

    private void updateSmppConnections() {
        smscAccountService.getAllDto()
                .forEach(smsc -> smppConnections.put(smsc.getSmscAccountId(), new SmppConnection(smsc)));
    }

}
