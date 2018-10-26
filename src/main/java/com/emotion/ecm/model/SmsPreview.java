package com.emotion.ecm.model;

import com.emotion.ecm.enums.PreviewStatus;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@Entity
@Table(name = "TB_SMS_PREVIEW")
public class SmsPreview {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "NAME")
    private String name;

    @Column(name = "CREATE_DATE")
    private LocalDateTime createDate;

    @Column(name = "UPDATE_DATE")
    private LocalDateTime updateDate;

    @Column(name = "SEND_DATE")
    private LocalDateTime sendDate;

    @Column(name = "TEXT")
    private String text;

    @Column(name = "TPS")
    private short tps;

    @Column(name = "RECIPIENTS_COUNT")
    private int recipientsCount;

    @Column(name = "PREVIEW_STATUS")
    @Enumerated(EnumType.ORDINAL)
    private PreviewStatus previewStatus;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ACCOUNT_ID")
    private Account account;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "USER_ID")
    private AppUser user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "SMS_PRIORITY_ID")
    private SmsPriority smsPriority;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "SMS_TYPE_ID")
    private SmsType smsType;

}
