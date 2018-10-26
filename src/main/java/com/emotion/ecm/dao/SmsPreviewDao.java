package com.emotion.ecm.dao;

import com.emotion.ecm.model.Account;
import com.emotion.ecm.model.AppUser;
import com.emotion.ecm.model.SmsPreview;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SmsPreviewDao extends JpaRepository<SmsPreview, Long> {

    List<SmsPreview> findAllByAccountAndUser(Account account, AppUser user);

}
