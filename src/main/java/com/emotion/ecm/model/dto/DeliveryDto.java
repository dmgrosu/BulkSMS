package com.emotion.ecm.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class DeliveryDto {

    private long messageDbId;
    private String messageId;
    private long maxTimeInMillis;

    public DeliveryDto(long messageDbId, String messageId) {
        this.messageDbId = messageDbId;
        this.messageId = messageId;
    }

}
