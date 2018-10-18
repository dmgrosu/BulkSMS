package com.emotion.ecm.service;

import com.emotion.ecm.dao.AppRoleDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AppRoleService {

    private AppRoleDao appRoleDao;

    @Autowired
    public AppRoleService(AppRoleDao appRoleDao) {
        this.appRoleDao = appRoleDao;
    }
}
