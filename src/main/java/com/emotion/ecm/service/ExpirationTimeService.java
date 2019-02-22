package com.emotion.ecm.service;

import com.emotion.ecm.dao.ExpirationTimeDao;
import com.emotion.ecm.exception.ExpirationTimeException;
import com.emotion.ecm.model.Account;
import com.emotion.ecm.model.ExpirationTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ExpirationTimeService {

    private ExpirationTimeDao expirationTimeDao;

    @Autowired
    public ExpirationTimeService(ExpirationTimeDao expirationTimeDao) {
        this.expirationTimeDao = expirationTimeDao;
    }

    public List<ExpirationTime> getAllByAccount(Account account) {
        return expirationTimeDao.findAllByAccount(account);
    }

    public ExpirationTime getById(int id) {
        return expirationTimeDao.getOne(id);
    }

    public String getExpirationTimeValueById(int id) throws ExpirationTimeException {
        if (id == 0) {
            throw new ExpirationTimeException("expiration time id is 0");
        }
        return expirationTimeDao.findValueById(id);
    }

}
