package com.emotion.ecm.dao;

import com.emotion.ecm.enums.PreviewStatus;
import com.emotion.ecm.model.AppUser;
import com.emotion.ecm.model.SmsPreview;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface SmsPreviewDao extends JpaRepository<SmsPreview, Long> {

    List<SmsPreview> findAllByUser(AppUser user);

    Optional<SmsPreview> findByUserIdAndName(int userId, String name);

    @Query("from SmsPreview " +
            "where user.id in (:userIds) and " +
            "sendDate <= :date and deleted = false and " +
            "previewStatus in (:statuses) " +
            "order by smsPriority asc")
    List<SmsPreview> findPreviewsForBroadcast(@Param("userIds") List<Integer> userIds,
                                              @Param("date") LocalDateTime currDate,
                                              @Param("statuses") List<PreviewStatus> statuses);

    List<SmsPreview> findAllByUserAccountIdInAndDeletedAndPreviewStatusInAndSendDateBefore(List<Integer> usersIds,
                                                                                           boolean deleted,
                                                                                           List<PreviewStatus> statuses,
                                                                                           LocalDateTime currDate);

}
