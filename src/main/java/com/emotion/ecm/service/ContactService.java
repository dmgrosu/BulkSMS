package com.emotion.ecm.service;

import com.emotion.ecm.dao.ContactDao;
import com.emotion.ecm.dao.GroupDao;
import com.emotion.ecm.model.AppUser;
import com.emotion.ecm.model.Contact;
import com.emotion.ecm.model.Group;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
public class ContactService {

    private ContactDao contactDao;
    private GroupDao groupDao;

    @Autowired
    public ContactService(ContactDao contactDao, GroupDao groupDao) {
        this.contactDao = contactDao;
        this.groupDao = groupDao;
    }

    public List<Contact> getAllContactsByGroups(Set<Group> groups) {
        return contactDao.findAllByGroups(groups);
    }

    public List<Group> getAllGroupsByUser(AppUser user) {
        return groupDao.findAllByUser(user);
    }

    public Group getGroupById(Integer groupId) {
        return groupDao.getOne(groupId);
    }
}
