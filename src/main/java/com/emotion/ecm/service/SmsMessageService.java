package com.emotion.ecm.service;

import com.emotion.ecm.dao.SmsMessageDao;
import com.emotion.ecm.model.SmsMessage;
import com.emotion.ecm.model.SmsPreview;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SmsMessageService {

    private SmsMessageDao smsMessageDao;

    @Autowired
    public SmsMessageService(SmsMessageDao smsMessageDao) {
        this.smsMessageDao = smsMessageDao;
    }

    public List<SmsMessage> getAllToBeSend(List<SmsPreview> previews) {
        return null;
    }

    @Transactional
    public List<SmsMessage> batchSave(List<SmsMessage> messages) {
        return smsMessageDao.saveAll(messages);
    }
}
