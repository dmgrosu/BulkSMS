package com.emotion.ecm.model.dto;

import com.emotion.ecm.model.SmsPrefix;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PrefixGroupDto {

    private int groupId;
    private int accountId;
    private String groupName;
    private List<SmsPrefix> prefixes;

}
