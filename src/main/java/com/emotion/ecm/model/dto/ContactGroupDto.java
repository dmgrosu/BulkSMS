package com.emotion.ecm.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import javax.validation.constraints.NotEmpty;

@Data
@AllArgsConstructor
public class ContactGroupDto {

    private int groupId;
    @NotEmpty
    private String groupName;
    private int totalContacts;
    private int userId;

    public ContactGroupDto() {
    }

    public ContactGroupDto(int groupId) {
        this.groupId = groupId;
    }

    public ContactGroupDto(int groupId, String groupName) {
        this.groupId = groupId;
        this.groupName = groupName;
    }

    public ContactGroupDto(int groupId, String groupName, int userId) {
        this.groupId = groupId;
        this.groupName = groupName;
        this.userId = userId;
    }

}
