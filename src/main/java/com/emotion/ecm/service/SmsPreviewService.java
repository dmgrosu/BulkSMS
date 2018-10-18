package com.emotion.ecm.service;

import com.emotion.ecm.dao.SmsPreviewDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SmsPreviewService {

    private SmsPreviewDao smsPreviewDao;

    @Autowired
    public SmsPreviewService(SmsPreviewDao smsPreviewDao) {
        this.smsPreviewDao = smsPreviewDao;
    }

}
