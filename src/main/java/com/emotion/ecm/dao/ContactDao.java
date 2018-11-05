package com.emotion.ecm.dao;

import com.emotion.ecm.model.Contact;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ContactDao extends JpaRepository<Contact, Integer> {
}
