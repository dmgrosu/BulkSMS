package com.emotion.ecm.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PrefixDto {

    private int prefixId;

    @NotNull
    private String prefix;

    @NotNull
    private int groupId;

}
