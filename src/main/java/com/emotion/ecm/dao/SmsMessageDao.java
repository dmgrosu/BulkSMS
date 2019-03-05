package com.emotion.ecm.dao;

import com.emotion.ecm.enums.MessageStatus;
import com.emotion.ecm.model.SmsMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.Set;

public interface SmsMessageDao extends JpaRepository<SmsMessage, Long> {

    @Query("select distinct destAddress from SmsMessage where preview.id = ?1 and messageStatus > 0")
    Set<String> getSentDestinationsByPreviewId(long previewId);

    @Modifying
    @Query("update SmsMessage " +
            "set messageId = ?2, submitRespDate = ?3, messageStatus = ?4 " +
            "where id = ?1")
    void updateMessageIdById(long id, String messageId, LocalDateTime submitRespDate, MessageStatus newStatus);

    @Modifying
    @Query("update SmsMessage set messageStatus = ?2 where id = ?1")
    void updateMessageStatusById(long id, MessageStatus status);

    @Modifying
    @Query("update SmsMessage set messageStatus = ?2, dlrDate = ?3 where id = ?1")
    void updateDlrDateById(long id, MessageStatus status, LocalDateTime dlrDate);

}
