package com.emotion.ecm.service;

import com.emotion.ecm.dao.ContactDao;
import com.emotion.ecm.dao.GroupDao;
import com.emotion.ecm.exception.ContactException;
import com.emotion.ecm.model.AppUser;
import com.emotion.ecm.model.Contact;
import com.emotion.ecm.model.Group;
import com.emotion.ecm.model.dto.ContactDto;
import com.emotion.ecm.model.dto.ContactGroupDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class ContactService {

    public static final Logger LOGGER = LoggerFactory.getLogger(ContactService.class);

    private ContactDao contactDao;
    private GroupDao groupDao;

    @Autowired
    public ContactService(ContactDao contactDao, GroupDao groupDao) {
        this.contactDao = contactDao;
        this.groupDao = groupDao;
    }

    public List<ContactDto> getAllContactDtoByUserId(AppUser user) {

        List<ContactGroupDto> groups = groupDao.findAllDtoByUserId(user.getId());
        Set<Integer> ids = groups.stream().map(ContactGroupDto::getGroupId).collect(Collectors.toSet());
        Set<ContactDto> dtoHashSet = new HashSet<>();
        if (!ids.isEmpty()) {
            dtoHashSet.addAll(contactDao.findAllDtoByGroupIds(ids));
        }
        return dtoHashSet.stream()
                .sorted(Comparator.comparing(ContactDto::getFullName))
                .collect(Collectors.toList());
    }

    public List<Group> getAllGroupsByUser(AppUser user) {
        return groupDao.findAllByUser(user);
    }

    public List<ContactGroupDto> getAllGroupDtoByUserId(int userId, boolean addContactsCount) {
        List<ContactGroupDto> result = groupDao.findAllDtoByUserId(userId);
        if (addContactsCount) {
            for (ContactGroupDto contactGroupDto : result) {
                contactGroupDto.setTotalContacts(contactDao.countByGroups_Id(contactGroupDto.getGroupId()));
            }
        }
        return result;
    }

    public ContactGroupDto getGroupDtoById(int groupId) throws ContactException {
        return groupDao.findDtoById(groupId);
    }

    public Group getGroupById(int groupId) {
        return groupDao.findById(groupId).get();
    }

    public List<String> getAllPhoneNumbersByGroups(Set<Integer> groupIds) {
        return contactDao.findAllPhoneNumbersByGroups(groupIds).stream()
                .map(ContactDto::getMobilePhone).collect(Collectors.toList());
    }

    public ContactDto getContactDtoById(int id) throws ContactException {
        ContactDto result = contactDao.findDtoById(id);
        getAllGroupIdByContactId(id)
                .forEach(groupId -> result.getGroups()
                        .add(new ContactGroupDto(groupId)));
        return result;
    }

    public Set<Integer> getAllGroupIdByContactId(int contactId) throws ContactException{
        Contact contact = contactDao.findById(contactId)
                .orElseThrow(() -> new ContactException("contact not found"));
        return contact.getGroups().stream()
                .map(Group::getId).collect(Collectors.toSet());
    }

    public Optional<Group> findGroupByNameAndUser(String groupName, AppUser user) {
        return groupDao.findByNameAndUser(groupName, user);
    }

    @Transactional
    public Group saveGroup(Group group) {
        return groupDao.save(group);
    }

    public Group saveGroup(ContactGroupDto dto, AppUser user) {
        return saveGroup(convertGroupDtoToGroup(dto, user));
    }

    @Transactional
    public Contact saveContact(Contact contact) {
        return contactDao.save(contact);
    }

    public Contact saveContact(ContactDto dto) {
        Set<Group> groups = new HashSet<>();
        for (ContactGroupDto groupDto : dto.getGroups()) {
            groupDao.findById(groupDto.getGroupId()).ifPresent(groups::add);
        }
        return saveContact(convertContactDtoToContact(dto, groups));
    }

    public boolean checkContactDuplicates(ContactDto dto, AppUser currUser) {
        Optional<Contact> optionalContact =
                contactDao.findByFirstNameAndLastNameAndMobilePhone(dto.getFirstName(),
                        dto.getLastName(), dto.getMobilePhone());
        if (optionalContact.isPresent()) {
            Contact contact = optionalContact.get();
            try {
                for (Group group : contact.getGroups()) {
                    if (group.getUser().equals(currUser)) {
                        return true;
                    }
                }
            } catch (Exception e) {
                LOGGER.error(e.getMessage());
            }
        }
        return false;
    }

    private Group convertGroupDtoToGroup(ContactGroupDto dto, AppUser user) {

        Group result = groupDao.findById(dto.getGroupId()).orElseGet(Group::new);
        result.setName(dto.getGroupName());
        result.setUser(user);

        return result;
    }

    private Contact convertContactDtoToContact(ContactDto dto, Set<Group> groups) {

        Contact result = contactDao.findById(dto.getContactId()).orElseGet(Contact::new);
        result.setFirstName(dto.getFirstName());
        result.setLastName(dto.getLastName());
        result.setMobilePhone(dto.getMobilePhone());
        Set<Group> originalGroups = result.getGroups();
        if (originalGroups == null) {
            originalGroups = new HashSet<>();
            result.setGroups(originalGroups);
        }
        originalGroups.addAll(groups);

        return result;
    }

    public void deleteGroupById(int groupId) {
        groupDao.deleteById(groupId);
    }

    public void deleteContactById(int contactId) {
        contactDao.deleteById(contactId);
    }
}
