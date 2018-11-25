package com.emotion.ecm.service;

import com.emotion.ecm.dao.SmppAddressDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SmppAddressService {

    private SmppAddressDao smppAddressDao;

    @Autowired
    public SmppAddressService(SmppAddressDao smppAddressDao) {
        this.smppAddressDao = smppAddressDao;
    }

}
