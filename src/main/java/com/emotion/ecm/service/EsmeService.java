package com.emotion.ecm.service;

import com.emotion.ecm.model.AppUser;
import com.emotion.ecm.model.SmsMessage;
import com.emotion.ecm.model.SmsPreview;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class EsmeService {

    private SmsPreviewService smsPreviewService;
    private SmsMessageService smsMessageService;
    private AppUserService userService;

    @Autowired
    public EsmeService(SmsPreviewService smsPreviewService,
                       SmsMessageService smsMessageService,
                       AppUserService userService) {
        this.smsPreviewService = smsPreviewService;
        this.smsMessageService = smsMessageService;
        this.userService = userService;
    }

    @Scheduled(fixedRate = 5000)
    public void sendMessages() {

        List<AppUser> users = new ArrayList<>();
        users.add(userService.getAuthenticatedUser());

        List<SmsPreview> previews = smsPreviewService.getPreviewsForBroadcast(users);
        if (previews.isEmpty()) {
            List<SmsMessage> messages = smsMessageService.getAllToBeSend(previews);

        }
    }
}
