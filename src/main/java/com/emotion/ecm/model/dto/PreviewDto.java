package com.emotion.ecm.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PreviewDto {

    private long previewId;
    private String name;
    private String createDate;
    private String sendDate;
    private String text;
    private short tps;
    private int recipientsCount;
    private String previewStatus;
    private int accountId;
    private String accountName;
    private String type;
    private String priority;
    private int userId;
    private String username;
    private String status;

}
