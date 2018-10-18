package com.emotion.ecm.service;

import com.emotion.ecm.dao.AppUserDao;
import com.emotion.ecm.model.AppUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AppUserService {

    private static final Logger LOG = LoggerFactory.getLogger(AppUserService.class);

    private AppUserDao appUserDao;

    @Autowired
    public AppUserService(AppUserDao appUserDao) {
        this.appUserDao = appUserDao;
    }

    public Optional<AppUser> getByUsername(String username) {
        Optional<AppUser> user = appUserDao.findByUsername(username);
        if (!user.isPresent()) {
            LOG.debug(String.format("User %s not found", username));
        }
        return appUserDao.findByUsername(username);
    }

}
