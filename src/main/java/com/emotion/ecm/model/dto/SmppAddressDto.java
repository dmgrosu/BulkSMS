package com.emotion.ecm.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.jsmpp.bean.NumberingPlanIndicator;
import org.jsmpp.bean.TypeOfNumber;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.validation.constraints.NotEmpty;

@Data
@AllArgsConstructor
public class SmppAddressDto {

    private int smppAddressId;

    @NotEmpty
    private String address;
    @Enumerated(EnumType.ORDINAL)
    private TypeOfNumber ton;
    @Enumerated(EnumType.STRING)
    private NumberingPlanIndicator npi;

    private int accountId;

    public SmppAddressDto() {
    }

    public SmppAddressDto(int smppAddressId, String address) {
        this.smppAddressId = smppAddressId;
        this.address = address;
    }
}
