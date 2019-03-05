package com.emotion.ecm.service;

import com.emotion.ecm.model.dto.AccountDto;
import com.emotion.ecm.model.dto.PreviewDto;
import com.emotion.ecm.model.dto.SmscAccountDto;
import com.emotion.ecm.model.dto.SubmitSmDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

@Service
public class EsmeService {

    @Value("${ecm.messagePackQueueSize}")
    private int maxQueueSize;

    private SmsPreviewService smsPreviewService;
    private SmsMessageService smsMessageService;
    private SmscAccountService smscAccountService;
    private AccountService accountService;
    private ConcurrentHashMap<Integer, ConcurrentLinkedQueue<SubmitSmDto[]>> messageQueue;

    private static final Logger LOGGER = LoggerFactory.getLogger(EsmeService.class);

    @Autowired
    public EsmeService(SmsPreviewService smsPreviewService, AccountService accountService,
                       SmsMessageService smsMessageService, SmscAccountService smscAccountService) {
        this.smsPreviewService = smsPreviewService;
        this.smsMessageService = smsMessageService;
        this.smscAccountService = smscAccountService;
        this.accountService = accountService;
        this.messageQueue = new ConcurrentHashMap<>();
    }

    @Async
    @Scheduled(fixedRate = 5000)
    public void fillMessageQueue() {

        if (messageQueue == null || messageQueue.isEmpty()) {
            initQueue();
        }

        if (messageQueue == null || messageQueue.isEmpty()) {
            LOGGER.error("error message queue initialization");
            return;
        }

        List<AccountDto> allAccounts = accountService.getAllDto();

        for (ConcurrentLinkedQueue<SubmitSmDto[]> queue : messageQueue.values()) {

            if (queue.size() >= maxQueueSize) {
                continue;
            }

            Map<PreviewDto, Integer> previews = smsPreviewService.getPreviewsForBroadcast(allAccounts);
            if (previews.isEmpty()) {
                continue;
            }

            previews = balanceTpsByPreviews(previews);

            while (queue.size() < maxQueueSize) {
                SubmitSmDto[] messagePack = smsMessageService.createMessagePackFromPreviewList(previews);
                if (messagePack.length > 0) {
                    queue.add(messagePack);
                } else {
                    break;
                }
            }
        }
    }

    public SubmitSmDto[] getMessagePackFromQueue(int smscAccountId) {

        SubmitSmDto[] result = null;

        if (messageQueue.containsKey(smscAccountId)) {
            result = messageQueue.get(smscAccountId).poll();
        }

        return result;
    }

    public void returnMessagePackInQueue(int smscAccountId, SubmitSmDto[] messagePack) {
        if (messageQueue.containsKey(smscAccountId)) {
            messageQueue.get(smscAccountId).add(messagePack);
        }
    }

    /**
     * Balances tps between broadcasting previews according to max allowed tps from SMSC
     *
     * @param previews - map of previews for broadcast(key) and SMSCAccountId(value)
     * @return new map: key - preview with adjusted tps, value - SMSCAccountId
     */
    private Map<PreviewDto, Integer> balanceTpsByPreviews(final Map<PreviewDto, Integer> previews) {

        Map<Integer, Integer> tpsBySmscInitial = new HashMap<>();

        for (Map.Entry<PreviewDto, Integer> previewEntry : previews.entrySet()) {
            int tpsFromPreview = previewEntry.getKey().getTps();
            Integer smscAccountId = previewEntry.getValue();
            if (tpsBySmscInitial.containsKey(smscAccountId)) {
                int newTpsValue = tpsBySmscInitial.get(smscAccountId) + tpsFromPreview;
                tpsBySmscInitial.put(smscAccountId, newTpsValue);
            } else {
                tpsBySmscInitial.put(smscAccountId, tpsFromPreview);
            }
        }

        // key - SMSCAccountId
        // value - tps ratio
        Map<Integer, Double> ratioMap = new HashMap<>();

        for (Integer smscId : tpsBySmscInitial.keySet()) {
            Integer maxAvailableTps = smscAccountService.getTpsById(smscId);
            Integer initialTps = tpsBySmscInitial.get(smscId);
            if (maxAvailableTps < initialTps) {
                ratioMap.put(smscId, Double.valueOf(maxAvailableTps) / initialTps);
            } else {
                ratioMap.put(smscId, 1.0);
            }
        }

        tpsBySmscInitial = null;

        Map<PreviewDto, Integer> result = new HashMap<>();

        for (Map.Entry<PreviewDto, Integer> previewEntry : previews.entrySet()) {
            int smscAccountId = previewEntry.getValue();
            double tpsPreviewRatio = ratioMap.get(smscAccountId);
            short currentTpsForPreview = previewEntry.getKey().getTps();
            Double newTpsForPreview = currentTpsForPreview * tpsPreviewRatio;
            PreviewDto previewDto = previewEntry.getKey();
            previewDto.setTps(newTpsForPreview.shortValue());
            result.put(previewDto, smscAccountId);
        }

        ratioMap = null;

        return result;
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
