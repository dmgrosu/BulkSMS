package com.emotion.ecm.dao;

import com.emotion.ecm.exception.ContactException;
import com.emotion.ecm.model.Contact;
import com.emotion.ecm.model.Group;
import com.emotion.ecm.model.dto.ContactDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface ContactDao extends JpaRepository<Contact, Integer> {

    Integer countByGroups_Id(int groupId);

    Optional<Contact> findByFirstNameAndLastNameAndMobilePhone(String firstName, String lastName, String mobilePhone);

    @Query("select distinct new com.emotion.ecm.model.dto.ContactDto" +
            "(c.id, c.firstName, c.lastName, c.mobilePhone) " +
            "from Contact c " +
            "join c.groups g " +
            "where g.id in (?1)")
    List<ContactDto> findAllDtoByGroupIds(List<Integer> groupIds);

    @Query("select new com.emotion.ecm.model.dto.ContactDto" +
            "(c.id, c.mobilePhone) " +
            "from Contact c " +
            "where c.groups = ?1")
    List<ContactDto> findAllPhoneNumbersByGroups(Set<Group> groups);

    @Query("select new com.emotion.ecm.model.dto.ContactDto" +
            "(c.id, c.firstName, c.lastName, c.mobilePhone) " +
            "from Contact c " +
            "where c.id = ?1")
    ContactDto findDtoById(int id) throws ContactException;

}
