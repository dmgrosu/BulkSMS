package com.emotion.ecm.service;

import com.emotion.ecm.dao.SmsTypeDao;
import com.emotion.ecm.model.SmsType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SmsTypeService {

    private SmsTypeDao smsTypeDao;

    @Autowired
    public SmsTypeService(SmsTypeDao smsTypeDao) {
        this.smsTypeDao = smsTypeDao;
    }

    public List<SmsType> getAll() {
        return smsTypeDao.findAll();
    }

}
