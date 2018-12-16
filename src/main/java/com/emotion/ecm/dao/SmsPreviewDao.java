package com.emotion.ecm.dao;

import com.emotion.ecm.model.AppUser;
import com.emotion.ecm.model.SmsPreview;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SmsPreviewDao extends JpaRepository<SmsPreview, Long> {

    List<SmsPreview> findAllByUser(AppUser user);

    Optional<SmsPreview> findByUserIdAndName(int userId, String name);
}
