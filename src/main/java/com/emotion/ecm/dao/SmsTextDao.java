package com.emotion.ecm.dao;

import com.emotion.ecm.model.SmsText;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SmsTextDao extends JpaRepository<SmsText, Integer> {

    Optional<SmsText> findByTextAndPartAndTotalParts(String text, short part, short totalParts);

}
