package com.emotion.ecm.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.*;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PreviewDto {

    private long previewId;

    @NotNull
    private String name;

    private String createDate;

    @NotNull
    private String sendDate;

    @NotNull
    private String text;

    @Min(1)
    @Max(200)
    private short tps;

    private int recipientsCount;
    private int totalParts;
    private int totalSent;
    private String status;
    private String phoneNumbers;

    @NotNull
    private int typeId;
    private String type;

    @NotNull
    private int priorityId;
    private String priority;

    @NotNull
    private int smppAddressId;
    private String smppAddress;

    @NotNull
    private int userId;
    private String username;

    @NotNull
    private int expirationTimeId;
    private String expirationTimeName;

    private boolean dlr;
    private int accountDataId;
    private String accountDataName;
    private Set<Integer> groupIds;

}
