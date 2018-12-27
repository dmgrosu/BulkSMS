package com.emotion.ecm.dao;

import com.emotion.ecm.model.SmsMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Set;

public interface SmsMessageDao extends JpaRepository<SmsMessage, Long> {

    @Query("select distinct destAddress from SmsMessage where preview.id = :previewId and messageStatus > 0")
    Set<String> getSentDestinationsByPreviewId(@Param("previewId") long previewId);

}
