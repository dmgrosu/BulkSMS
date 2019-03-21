package com.emotion.ecm.dao;

import com.emotion.ecm.enums.MessageStatus;
import com.emotion.ecm.model.SmsMessage;
import com.emotion.ecm.model.dto.report.MessageCount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;
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

    @Query("select new com.emotion.ecm.model.dto.report.MessageCount(m.messageStatus, count(m.id)) " +
            "from SmsMessage m " +
            "where m.submitRespDate is null or m.submitRespDate between ?1 and ?2 " +
            "group by m.messageStatus")
    List<MessageCount> getMessageCountByStatus(LocalDateTime startDate, LocalDateTime endDate);

    @Query("select new com.emotion.ecm.model.dto.report.MessageCount(m.messageStatus, count(m.id)) " +
            "from SmsMessage m " +
            "join m.preview.user.account a " +
            "where (m.submitRespDate is null or m.submitRespDate between ?2 and ?3) and " +
            "a.id = ?1 " +
            "group by m.messageStatus")
    List<MessageCount> getMessageCountByStatusByAccount(Integer accountId, LocalDateTime startDate,
                                                        LocalDateTime endDate);

    @Query("select new com.emotion.ecm.model.dto.report.MessageCount(m.messageStatus, count(m.id)) " +
            "from SmsMessage m " +
            "join m.preview.user u " +
            "where (m.submitRespDate is null or m.submitRespDate between ?2 and ?3) and " +
            "u.id = ?1 " +
            "group by m.messageStatus")
    List<MessageCount> getMessageCountByStatusByUser(Integer userId, LocalDateTime startDate,
                                                     LocalDateTime endDate);

    @Query("select new com.emotion.ecm.model.dto.report.MessageCount(m.preview.smppAddress.address, count(m.id)) " +
            "from SmsMessage m " +
            "where m.submitRespDate is null or m.submitRespDate between ?1 and ?2 " +
            "group by m.preview.smppAddress.address")
    List<MessageCount> getMessageCountBySmppAddress(LocalDateTime startDate, LocalDateTime endDate);

    @Query("select new com.emotion.ecm.model.dto.report.MessageCount(m.preview.smppAddress.address, count(m.id)) " +
            "from SmsMessage m " +
            "join m.preview.user.account a " +
            "where (m.submitRespDate is null or m.submitRespDate between ?2 and ?3) and " +
            "a.id = ?1 " +
            "group by m.preview.smppAddress.address")
    List<MessageCount> getMessageCountBySmppAddressByAccount(Integer accountId, LocalDateTime startDate,
                                                             LocalDateTime endDate);

    @Query("select new com.emotion.ecm.model.dto.report.MessageCount(m.preview.smppAddress.address, count(m.id)) " +
            "from SmsMessage m " +
            "join m.preview.user u " +
            "where (m.submitRespDate is null or m.submitRespDate between ?2 and ?3) and " +
            "u.id = ?1 " +
            "group by m.preview.smppAddress.address")
    List<MessageCount> getMessageCountBySmppAddressByUser(Integer userId, LocalDateTime startDate,
                                                          LocalDateTime endDate);

    @Query("select new com.emotion.ecm.model.dto.report.MessageCount(m.smsPrefix.prefix, count(m.id)) " +
            "from SmsMessage m " +
            "where m.submitRespDate is null or m.submitRespDate between ?1 and ?2 " +
            "group by m.smsPrefix.prefix")
    List<MessageCount> getMessageCountByPrefixes(LocalDateTime startDate, LocalDateTime endDate);

    @Query("select new com.emotion.ecm.model.dto.report.MessageCount(m.smsPrefix.prefix, count(m.id)) " +
            "from SmsMessage m " +
            "join m.preview.user.account a " +
            "where (m.submitRespDate is null or m.submitRespDate between ?2 and ?3) and " +
            "a.id = ?1 " +
            "group by m.smsPrefix.prefix")
    List<MessageCount> getMessageCountByPrefixesByAccount(Integer accountId, LocalDateTime startDate,
                                                          LocalDateTime endDate);

    @Query("select new com.emotion.ecm.model.dto.report.MessageCount(m.smsPrefix.prefix, count(m.id)) " +
            "from SmsMessage m " +
            "join m.preview.user u " +
            "where (m.submitRespDate is null or m.submitRespDate between ?2 and ?3) and " +
            "u.id = ?1 " +
            "group by m.smsPrefix.prefix")
    List<MessageCount> getMessageCountByPrefixesByUser(Integer userId, LocalDateTime startDate,
                                                       LocalDateTime endDate);

    @Query("select new com.emotion.ecm.model.dto.report.MessageCount(m.preview.name, count(m.id)) " +
            "from SmsMessage m " +
            "where m.submitRespDate is null or m.submitRespDate between ?1 and ?2 " +
            "group by m.preview.name")
    List<MessageCount> getMessageCountByPreview(LocalDateTime startDate, LocalDateTime endDate);

    @Query("select new com.emotion.ecm.model.dto.report.MessageCount(m.preview.name, count(m.id)) " +
            "from SmsMessage m " +
            "join m.preview.user.account a " +
            "where (m.submitRespDate is null or m.submitRespDate between ?2 and ?3) and " +
            "a.id = ?1 " +
            "group by m.preview.name")
    List<MessageCount> getMessageCountByPreviewByAccount(Integer accountId, LocalDateTime startDate,
                                                         LocalDateTime endDate);

    @Query("select new com.emotion.ecm.model.dto.report.MessageCount(m.preview.name, count(m.id)) " +
            "from SmsMessage m " +
            "join m.preview.user u " +
            "where (m.submitRespDate is null or m.submitRespDate between ?2 and ?3) and " +
            "u.id = ?1 " +
            "group by m.preview.name")
    List<MessageCount> getMessageCountByPreviewByUser(Integer userId, LocalDateTime startDate,
                                                      LocalDateTime endDate);
}
