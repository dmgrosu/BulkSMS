package com.emotion.ecm.service;

import com.emotion.ecm.dao.SmppAddressDao;
import com.emotion.ecm.model.Account;
import com.emotion.ecm.model.SmppAddress;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SmppAddressService {

    private SmppAddressDao smppAddressDao;

    @Autowired
    public SmppAddressService(SmppAddressDao smppAddressDao) {
        this.smppAddressDao = smppAddressDao;
    }

    public List<SmppAddress> getAllByAccount(Account account) {
        return smppAddressDao.findAllByAccount(account);
    }

}
