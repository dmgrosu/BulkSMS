package com.emotion.ecm.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;

@Data
@AllArgsConstructor
public class AccountDto {

    private int accountId;
    @NotEmpty
    private String name;
    @Min(1)
    @Max(200)
    private short tps;
    @Min(1)
    private int smscAccountId;
    private String smscAccountSystemId;

    public AccountDto() {
    }

    public AccountDto(int accountId, String name) {
        this.accountId = accountId;
        this.name = name;
    }

}
