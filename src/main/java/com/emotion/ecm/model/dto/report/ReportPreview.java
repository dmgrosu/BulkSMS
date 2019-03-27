package com.emotion.ecm.model.dto.report;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReportPreview {
    private LocalDateTime sendDate;
    private String previewName;
    private String accountName;
    private String username;
    private String text;
}
