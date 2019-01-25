package com.emotion.ecm.service;

import com.emotion.ecm.model.Account;
import com.emotion.ecm.model.SmsPreview;
import com.emotion.ecm.model.dto.SmscAccountDto;
import com.emotion.ecm.model.dto.SubmitSmDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;

@Service
public class EsmeService {

    private SmsPreviewService smsPreviewService;
    private SmsMessageService smsMessageService;
    private SmscAccountService smscAccountService;
    private AccountService accountService;
    private Map<Integer, ConcurrentLinkedQueue<SubmitSmDto[]>> messageQueue;

    private static final Logger LOGGER = LoggerFactory.getLogger(EsmeService.class);

    @Autowired
    public EsmeService(SmsPreviewService smsPreviewService, AccountService accountService,
                       SmsMessageService smsMessageService, SmscAccountService smscAccountService) {
        this.smsPreviewService = smsPreviewService;
        this.smsMessageService = smsMessageService;
        this.smscAccountService = smscAccountService;
        this.accountService = accountService;
        this.messageQueue = new HashMap<>();
    }

    //@Scheduled(fixedRate = 5000)
    public void fillMessageQueue() {

        if (messageQueue == null || messageQueue.isEmpty()) {
            initQueue();
        }

        if (messageQueue == null || messageQueue.isEmpty()) {
            LOGGER.error("error message queue initialization");
            return;
        }

        List<Account> allAccounts = accountService.getAll();

        for (Integer smscAccountId : messageQueue.keySet()) {
            List<SmsPreview> previews = smsPreviewService.getPreviewsForBroadcast(allAccounts);
            if (previews.isEmpty()) {
                continue;
            }
            List<SubmitSmDto> submitSmList = smsMessageService.getAllToBeSendByPreview(previews,
                    smscAccountService.getById(smscAccountId));
        }
    }

    private void initQueue() {

        List<SmscAccountDto> allSmscAccounts = smscAccountService.getAllDto();
        if (allSmscAccounts.isEmpty()) {
            LOGGER.error("no available Smsc accounts found");
            return;
        }

        for (SmscAccountDto dto : allSmscAccounts) {
            messageQueue.put(dto.getSmscAccountId(), new ConcurrentLinkedQueue<>());
        }

    }

}
