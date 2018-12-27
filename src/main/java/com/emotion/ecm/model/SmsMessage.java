package com.emotion.ecm.model;

import com.emotion.ecm.enums.MessageStatus;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;
import org.springframework.data.domain.Persistable;

import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@Entity
@Table(name = "TB_SMS_MESSAGE")
@SQLDelete(sql = "UPDATE TB_SMS_MESSAGE SET deleted=true WHERE id=?")
@Where(clause = "deleted=false")
public class SmsMessage {

    @Id
    @GeneratedValue(generator = "incrementGenerator")
    @GenericGenerator(name = "incrementGenerator", strategy = "increment")
    private long id;

    @Column(name = "DEST_ADDR")
    private String destAddress;

    @Column(name = "SUBM_DATE")
    private LocalDateTime submitDate;

    @Column(name = "SUBM_RESP_DATE")
    private LocalDateTime submitRespDate;

    @Column(name = "DLR_DATE")
    private LocalDateTime dlrDate;

    @Column(name = "MSG_STATUS")
    @Enumerated(EnumType.ORDINAL)
    private MessageStatus messageStatus;

    @Column(name = "MESSAGE_ID")
    private String messageId;

    @Column(name = "DELETED")
    private boolean deleted;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "SMS_PREVIEW_ID")
    private SmsPreview preview;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "SMS_TEXT_ID")
    private SmsText smsText;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "SMS_PREFIX_ID")
    private SmsPrefix smsPrefix;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "SMSC_ACCOUNT_ID")
    private SmscAccount smscAccount;

}
