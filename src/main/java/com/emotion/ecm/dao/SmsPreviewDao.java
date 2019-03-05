package com.emotion.ecm.dao;

import com.emotion.ecm.enums.PreviewStatus;
import com.emotion.ecm.model.AppUser;
import com.emotion.ecm.model.SmsPreview;
import com.emotion.ecm.model.dto.PreviewDto;
import com.emotion.ecm.model.dto.PreviewGroupDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface SmsPreviewDao extends JpaRepository<SmsPreview, Long> {

    List<SmsPreview> findAllByUser(AppUser user);

    Optional<SmsPreview> findByUserIdAndName(int userId, String name);

    @Query("select new com.emotion.ecm.model.dto.PreviewDto" +
            "(id, sendDate, text, tps, sentParts, previewStatus, phoneNumbers, textEdited, smsType.id, " +
            "smsPriority.id, smppAddress.id, user.id, expirationTime.id, dlr, accountData.id) " +
            "from SmsPreview " +
            "where user.id in (?1) and " +
            "sendDate <= ?2 and deleted = false and " +
            "previewStatus in (?3) and finishDate is null")
    List<PreviewDto> findPreviewDtoForBroadcast(List<Integer> userIds, LocalDateTime currDate,
                                                List<PreviewStatus> statuses);

    @Query("select new com.emotion.ecm.model.dto.PreviewDto" +
            "(id, name, sendDate, text, tps, recipientsCount, totalParts, sentParts, " +
            "previewStatus, smsPriority.name, smppAddress.address, user.username, " +
            "expirationTime.name) " +
            "from SmsPreview " +
            "where user.id = ?1 " +
            "and finishDate is null " +
            "order by sendDate desc")
    List<PreviewDto> findAllNotFinishedDtoByUserId(int id);

    @Query("select new com.emotion.ecm.model.dto.PreviewDto" +
            "(id, name, sendDate, text, tps, recipientsCount, totalParts, sentParts, " +
            "previewStatus, smsPriority.name, smppAddress.address, user.username, " +
            "expirationTime.name) " +
            "from SmsPreview " +
            "where user.id = ?1 " +
            "order by sendDate desc")
    List<PreviewDto> findAllDtoByUserId(int id);

    @Query("select new com.emotion.ecm.model.dto.PreviewDto" +
            "(id, name, createDate, sendDate, text, tps, previewStatus, phoneNumbers, " +
            "smsType.id, smsPriority.id, smppAddress.id, user.id, " +
            "expirationTime.id, dlr, accountData.id, recipientsCount, totalParts) " +
            "from SmsPreview " +
            "where id = ?1")
    PreviewDto findDtoById(long previewId);

    PreviewGroupDto findPreviewGroupDtoById(long previewId);

    @Modifying
    @Query("update SmsPreview set finishDate = ?2, previewStatus = ?3 where id = ?1")
    void updatePreviewToCompleted(long previewId, LocalDateTime finishDate, PreviewStatus newStatus);

    @Modifying
    @Query("update SmsPreview set sentParts = sentParts + ?2, previewStatus = ?3 where id = ?1")
    void updatePreviewSentCountById(long previewId, int sentCount, PreviewStatus newStatus);

    @Modifying
    @Query("update SmsPreview set previewStatus = ?2 where id = ?1")
    void updatePreviewStatusById(long previewId, PreviewStatus newStatus);
}
