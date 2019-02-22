package com.emotion.ecm.model.dto;

import com.emotion.ecm.enums.MessageStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SmsMessageDto {

    private long id;
    private String destAddress;
    private LocalDateTime submRespDate;
    private LocalDateTime dlrDate;
    private MessageStatus messageStatus;
    private String messageId;

    private Long previewId;
    private Integer smsTextId;
    private Integer smsPrefixId;
    private Integer smscAccountId;

}
