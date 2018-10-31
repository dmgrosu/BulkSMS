package com.emotion.ecm.service;

import com.emotion.ecm.dao.SmsPriorityDao;
import com.emotion.ecm.model.SmsPriority;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SmsPriorityService {

    private SmsPriorityDao smsPriorityDao;

    @Autowired
    public SmsPriorityService(SmsPriorityDao smsPriorityDao) {
        this.smsPriorityDao = smsPriorityDao;
    }

    public List<SmsPriority> getAll() {
        return smsPriorityDao.findAll();
    }
}
