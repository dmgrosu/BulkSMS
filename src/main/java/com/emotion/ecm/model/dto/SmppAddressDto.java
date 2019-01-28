package com.emotion.ecm.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.jsmpp.bean.NumberingPlanIndicator;
import org.jsmpp.bean.TypeOfNumber;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Data
@AllArgsConstructor
public class SmppAddressDto {

    private int smppAddressId;

    @NotEmpty
    private String address;
    @NotNull
    private TypeOfNumber ton;
    @NotNull
    private NumberingPlanIndicator npi;

    private int accountId;

    public SmppAddressDto() {
    }

    public SmppAddressDto(int smppAddressId, String address) {
        this.smppAddressId = smppAddressId;
        this.address = address;
    }
}
