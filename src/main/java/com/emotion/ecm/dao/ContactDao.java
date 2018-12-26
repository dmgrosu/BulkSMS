package com.emotion.ecm.dao;

import com.emotion.ecm.model.Contact;
import com.emotion.ecm.model.Group;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Set;

public interface ContactDao extends JpaRepository<Contact, Integer> {

    List<Contact> findAllByGroups(Set<Group> groups);

}
