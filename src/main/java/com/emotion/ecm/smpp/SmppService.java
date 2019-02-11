package com.emotion.ecm.smpp;

import com.emotion.ecm.exception.SendingException;
import com.emotion.ecm.model.dto.SmscAccountDto;
import com.emotion.ecm.model.dto.SubmitSmDto;
import com.emotion.ecm.service.SmscAccountService;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class SmppService {

    private static final Logger LOGGER = LoggerFactory.getLogger(SmppService.class);

    @Getter
    private ConcurrentHashMap<Integer, SmppConnection> smppConnections = new ConcurrentHashMap<>();
    private SmscAccountService smscAccountService;

    @Autowired
    public SmppService(SmscAccountService smscAccountService) {
        this.smscAccountService = smscAccountService;
    }

    //@Scheduled(fixedRate = 10000)
    public void updateSmppConnections() {

        List<SmscAccountDto> smscAccounts = smscAccountService.getAllDto();

        smppConnections.clear();

        smscAccounts.stream()
                .filter(smscAccount -> smppConnections.containsKey(smscAccount.getSmscAccountId()))
                .forEach(smscAccount -> smppConnections.put(smscAccount.getSmscAccountId(), new SmppConnection(smscAccount)));
    }

    public void sendMessage(SmppConnection smppConnection, SubmitSmDto[] smsMessages) {
        try {
            smppConnection.sendBulkMessages(smsMessages);
        } catch (SendingException e) {
            LOGGER.warn("Can't send messages: ", e);
        }
    }
}
