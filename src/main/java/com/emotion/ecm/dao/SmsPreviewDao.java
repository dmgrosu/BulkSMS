package com.emotion.ecm.dao;

import com.emotion.ecm.enums.PreviewStatus;
import com.emotion.ecm.model.AppUser;
import com.emotion.ecm.model.SmsPreview;
import com.emotion.ecm.model.dto.PreviewDto;
import com.emotion.ecm.model.dto.PreviewGroupDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface SmsPreviewDao extends JpaRepository<SmsPreview, Long> {

    List<SmsPreview> findAllByUser(AppUser user);

    Optional<SmsPreview> findByUserIdAndName(int userId, String name);

    @Query("select new com.emotion.ecm.model.dto.PreviewDto" +
            "(p.id, p.sendDate, p.text, p.tps, p.previewStatus, p.phoneNumbers, p.smsType.id, " +
            "p.smsPriority.id, p.smppAddress.id, p.user.id, p.expirationTime.id, p.dlr, " +
            "p.accountData.id) " +
            "from SmsPreview p " +
            "where p.user.id in (?1) and " +
            "sendDate <= ?2 and deleted = false and " +
            "previewStatus in (?3) ")
    List<PreviewDto> findPreviewDtoForBroadcast(List<Integer> userIds, LocalDateTime currDate,
                                                List<PreviewStatus> statuses);

    @Query("select new com.emotion.ecm.model.dto.PreviewDto" +
            "(p.id, p.name, p.sendDate, p.tps, p.recipientsCount, p.totalParts, p.sentParts, " +
            "p.previewStatus, p.smsPriority.name, p.smppAddress.address, p.user.username, " +
            "p.expirationTime.name) " +
            "from SmsPreview p " +
            "where p.user.id = ?1")
    List<PreviewDto> findAllDtoByUserId(int id);

    @Query("select new com.emotion.ecm.model.dto.PreviewDto" +
            "(p.id, p.name, p.createDate, p.sendDate, p.text, p.tps, p.previewStatus, p.phoneNumbers, " +
            "p.smsType.id, p.smsPriority.id, p.smppAddress.id, p.user.id, " +
            "p.expirationTime.id, p.dlr, p.accountData.id, p.recipientsCount, p.totalParts) " +
            "from SmsPreview p " +
            "where p.id = ?1")
    PreviewDto findDtoById(long previewId);

    PreviewGroupDto findPreviewGroupDtoById(long previewId);
}
