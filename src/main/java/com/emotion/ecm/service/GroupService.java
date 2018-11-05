package com.emotion.ecm.service;

import com.emotion.ecm.dao.GroupDao;
import com.emotion.ecm.model.AppUser;
import com.emotion.ecm.model.Group;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GroupService {

    private GroupDao groupDao;

    @Autowired
    public GroupService(GroupDao groupDao) {
        this.groupDao = groupDao;
    }

    public List<Group> getAllByUser(AppUser user) {
        return groupDao.findAllByUser(user);
    }
}
