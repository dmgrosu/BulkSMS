package com.emotion.ecm.dao;

import com.emotion.ecm.model.SmsMessage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SmsMessageDao extends JpaRepository<SmsMessage, Long> {

}
