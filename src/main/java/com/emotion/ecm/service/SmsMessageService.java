package com.emotion.ecm.service;

import com.emotion.ecm.dao.SmsMessageDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SmsMessageService {

    private SmsMessageDao smsMessageDao;

    @Autowired
    public SmsMessageService(SmsMessageDao smsMessageDao) {
        this.smsMessageDao = smsMessageDao;
    }
}
