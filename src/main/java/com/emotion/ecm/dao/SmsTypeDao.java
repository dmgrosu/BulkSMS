package com.emotion.ecm.dao;

import com.emotion.ecm.model.SmsType;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SmsTypeDao extends JpaRepository<SmsType, Integer> {
}
