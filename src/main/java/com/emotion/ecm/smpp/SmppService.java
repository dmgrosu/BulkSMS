package com.emotion.ecm.smpp;

import com.emotion.ecm.exception.SendingException;
import com.emotion.ecm.model.SmscAccount;
import com.emotion.ecm.model.dto.SubmitSmDto;
import com.emotion.ecm.service.SmscAccountService;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SmppService {
    private static final Logger LOGGER = LoggerFactory.getLogger(SmppService.class);

    @Getter
    private List<SmppConnection> smppConnections;

    @Autowired
    private SmscAccountService smscAccountService;

    @Scheduled(fixedRate = 10000)
    public void updateSmppConnections() {
        List<SmscAccount> smscAccounts = smscAccountService.getAll();
        List<Integer> smscIds = smscAccounts.stream()
                .mapToInt(SmscAccount::getId)
                .boxed()
                .collect(Collectors.toList());

        smppConnections.stream()
                .filter(smppConnection -> !smscIds.contains(smppConnection.getSmscAccount().getId()))
                .forEach(SmppConnection::shutdownConnection);

        smscAccounts.forEach(account -> smppConnections.add(new SmppConnection(account)));

    }


    public void sendMessage(SmppConnection smppConnection, SubmitSmDto[] smsMessages) {
        try {
            smppConnection.sendBulkMessages(smsMessages);
        } catch (SendingException e) {
            LOGGER.warn("Can't send messages: ", e);
        }
    }
}
