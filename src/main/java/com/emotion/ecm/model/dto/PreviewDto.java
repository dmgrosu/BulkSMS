package com.emotion.ecm.model.dto;

import com.emotion.ecm.enums.PreviewStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Data
@AllArgsConstructor
public class PreviewDto {

    private long previewId;

    @NotNull
    @NotEmpty
    private String name;

    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime createDate;

    @NotNull
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime sendDate;

    @NotNull
    @NotEmpty
    private String text;

    @Min(1)
    @Max(200)
    private short tps;

    private int recipientsCount;
    private int totalParts;
    private int totalSent;
    private PreviewStatus status;
    private String phoneNumbers;
    private boolean textEdited;

    @NotNull
    private Integer typeId;
    private String type;

    @NotNull
    private Integer priorityId;
    private String priority;

    @NotNull
    private Integer smppAddressId;
    private String smppAddress;

    @NotNull
    private Integer userId;
    private String username;

    @NotNull
    private Integer expirationTimeId;
    private String expirationTimeName;

    private boolean dlr;
    private Integer accountDataId;
    private String accountDataName;
    private Set<Integer> groupIds = new HashSet<>();

    public PreviewDto() {
    }

    public PreviewDto(long previewId, String name, LocalDateTime sendDate, String text, short tps,
                      int recipientsCount, int totalParts, int totalSent, PreviewStatus status,
                      String priority, String smppAddress, String username, String expirationTimeName) {
        this.previewId = previewId;
        this.name = name;
        this.sendDate = sendDate;
        this.text = text;
        this.tps = tps;
        this.recipientsCount = recipientsCount;
        this.totalParts = totalParts;
        this.totalSent = totalSent;
        this.status = status;
        this.priority = priority;
        this.smppAddress = smppAddress;
        this.username = username;
        this.expirationTimeName = expirationTimeName;
    }

    public PreviewDto(long previewId, LocalDateTime sendDate, String text, short tps, int totalSent,
                      PreviewStatus status, String phoneNumbers, boolean textEdited, int typeId, int priorityId,
                      int smppAddressId, int userId, int expirationTimeId, boolean dlr, Integer accountDataId) {
        this.previewId = previewId;
        this.sendDate = sendDate;
        this.text = text;
        this.tps = tps;
        this.totalSent = totalSent;
        this.status = status;
        this.phoneNumbers = phoneNumbers;
        this.textEdited = textEdited;
        this.typeId = typeId;
        this.priorityId = priorityId;
        this.smppAddressId = smppAddressId;
        this.userId = userId;
        this.expirationTimeId = expirationTimeId;
        this.dlr = dlr;
        this.accountDataId = accountDataId;
    }

    public PreviewDto(long previewId, String name, LocalDateTime createDate, LocalDateTime sendDate, String text,
                      short tps, PreviewStatus status, String phoneNumbers, int typeId, int priorityId,
                      int smppAddressId, int userId, int expirationTimeId, boolean dlr,
                      Integer accountDataId, int recipientsCount, int totalParts) {
        this.previewId = previewId;
        this.name = name;
        this.createDate = createDate;
        this.sendDate = sendDate;
        this.text = text;
        this.tps = tps;
        this.status = status;
        this.phoneNumbers = phoneNumbers;
        this.typeId = typeId;
        this.priorityId = priorityId;
        this.smppAddressId = smppAddressId;
        this.userId = userId;
        this.expirationTimeId = expirationTimeId;
        this.dlr = dlr;
        this.accountDataId = accountDataId;
        this.recipientsCount = recipientsCount;
        this.totalParts = totalParts;
    }

}
