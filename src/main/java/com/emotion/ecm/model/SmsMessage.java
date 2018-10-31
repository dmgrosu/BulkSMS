package com.emotion.ecm.model;

import com.emotion.ecm.enums.MessageStatus;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Where;

import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@Entity
@Table(name = "TB_SMS_MESSAGE")
@Where(clause = "DELETED=0")
public class SmsMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "DEST_ADDR")
    private String destAddress;

    @Column(name = "SEND_DATE")
    private LocalDateTime sendDate;

    @Column(name = "SUBM_DATE")
    private LocalDateTime submissionDate;

    @Column(name = "MSG_STAT")
    @Enumerated(EnumType.ORDINAL)
    private MessageStatus messageStatus;

    @Column(name = "SEQ_NUMB")
    private String sequenceNumber;

    @Column(name = "DELETED")
    private boolean deleted;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "SMS_PREVIEW_ID")
    private SmsPreview preview;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "SMS_TEXT_ID")
    private SmsText smsText;

}
