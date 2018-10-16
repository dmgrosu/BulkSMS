package com.emotion.ecm.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@Entity
@Table(name = "TB_SMS_MESSAGE")
public class SmsMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "SEND_DATE")
    private LocalDateTime sendDate;

    @Column(name = "SUBM_DATE")
    private LocalDateTime submissionDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "SMS_PREVIEW_ID")
    private SmsPreview preview;

}
