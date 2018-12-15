package com.emotion.ecm.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SmppAddressDto {

    private int smppAddressId;

    @NotNull
    private String address;

    private byte ton;
    private byte npi;
    private int accountId;

}
