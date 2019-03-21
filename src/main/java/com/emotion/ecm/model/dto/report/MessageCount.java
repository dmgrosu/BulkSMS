package com.emotion.ecm.model.dto.report;

import com.emotion.ecm.enums.MessageStatus;
import lombok.Data;

@Data
public class MessageCount {
    private String criteriaName;
    private Long count;

    public MessageCount(String criteriaName, Long count) {
        this.criteriaName = criteriaName;
        this.count = count;
    }

    public MessageCount(MessageStatus status, Long count) {
        this.criteriaName = status.name();
        this.count = count;
    }
}
