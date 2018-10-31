package com.emotion.ecm.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PreviewDto {

    private long previewId;

    @NotNull
    private String name;

    @NotNull
    private String createDate;

    @NotNull
    private String sendDate;

    @NotNull
    @Size(max = 160)
    private String text;

    @Min(1)
    @Max(200)
    private short tps;
    private int recipientsCount;
    private String previewStatus;
    private int accountId;
    private String accountName;
    private int typeId;
    private String type;
    private int priorityId;
    private String priority;
    private int userId;
    private String username;
    private String status;
    private String expirationTime;
    private boolean dlr;

}
