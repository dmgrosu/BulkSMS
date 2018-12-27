package com.emotion.ecm.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jsmpp.bean.NumberingPlanIndicator;
import org.jsmpp.bean.TypeOfNumber;

import javax.validation.constraints.NotNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SmppAddressDto {

    private int smppAddressId;

    @NotNull
    private String address;

    private TypeOfNumber ton;
    private NumberingPlanIndicator npi;
    private int accountId;

}
