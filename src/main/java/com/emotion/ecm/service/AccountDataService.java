package com.emotion.ecm.service;

import com.emotion.ecm.dao.AccountDataDao;
import com.emotion.ecm.model.AccountData;
import com.emotion.ecm.model.AppUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AccountDataService {

    private AccountDataDao accountDataDao;

    @Autowired
    public AccountDataService(AccountDataDao accountDataDao) {
        this.accountDataDao = accountDataDao;
    }

    public List<AccountData> getAllByUser(AppUser user) {
        return accountDataDao.findAllByUser(user);
    }
}
