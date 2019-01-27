package com.emotion.ecm.model.dto;

import com.emotion.ecm.model.SmsPrefix;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
public class PrefixGroupDto {

    private int groupId;
    private int accountId;
    private String groupName;
    private List<SmsPrefix> prefixes;

    public PrefixGroupDto() {
    }

    public PrefixGroupDto(int groupId, String groupName, int accountId) {
        this.groupId = groupId;
        this.groupName = groupName;
        this.accountId = accountId;
    }

}
