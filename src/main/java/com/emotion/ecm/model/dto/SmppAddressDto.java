package com.emotion.ecm.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SmppAddressDto {

    private int smppAddressId;
    private String address;
    private byte ton;
    private byte npi;
    private int accountId;

}
