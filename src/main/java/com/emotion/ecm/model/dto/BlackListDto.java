package com.emotion.ecm.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import javax.validation.constraints.NotEmpty;

@Data
@AllArgsConstructor
public class BlackListDto {

    private int blackListId;

    @NotEmpty
    private String name;

    private Integer accountId;
    private String accountName;

    public BlackListDto() {
    }

    public BlackListDto(int blackListId, String name, Integer accountId) {
        this.blackListId = blackListId;
        this.name = name;
        this.accountId = accountId;
    }
}
