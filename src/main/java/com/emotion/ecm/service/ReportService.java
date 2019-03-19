package com.emotion.ecm.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ReportService {

    private SmsMessageService smsMessageService;
    private SmsPreviewService smsPreviewService;

    @Autowired
    public ReportService(SmsMessageService smsMessageService, SmsPreviewService smsPreviewService) {
        this.smsMessageService = smsMessageService;
        this.smsPreviewService = smsPreviewService;
    }
}
