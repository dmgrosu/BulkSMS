package com.emotion.ecm.model.dto.report;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReportGeneral {

    private String period;
    private String accountName;
    private String username;
    private Long totalCount;
    private Long totalSentCount;
    private Long totalNotSentCount;
    private List<MessageCount> countByStatus = new ArrayList<>();
    private List<MessageCount> countBySmppAddress = new ArrayList<>();
    private List<MessageCount> countByPrefix = new ArrayList<>();
    private List<MessageCount> countByPreview = new ArrayList<>();

}
