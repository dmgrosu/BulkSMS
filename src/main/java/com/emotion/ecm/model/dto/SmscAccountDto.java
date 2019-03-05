package com.emotion.ecm.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;

@Data
@AllArgsConstructor
public class SmscAccountDto {

    private int smscAccountId;

    @NotEmpty
    private String systemId;

    @NotEmpty
    private String password;

    @NotEmpty
    private String ipAddress;

    private String systemType;

    @Min(1)
    @Max(65535)
    private int port;

    @Min(1)
    @Max(500)
    private short tps;

    @Min(1)
    @Max(10)
    private byte maxConnections;

    private boolean asynchronous;

    public SmscAccountDto() {
    }

    public SmscAccountDto(int smscAccountId, String systemId, String ipAddress) {
        this.smscAccountId = smscAccountId;
        this.systemId = systemId;
        this.ipAddress = ipAddress;
    }

}
