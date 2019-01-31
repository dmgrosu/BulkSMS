package com.emotion.ecm.model.dto;

import com.emotion.ecm.validation.ValidPhoneNumber;
import lombok.AllArgsConstructor;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import java.util.HashSet;
import java.util.Set;

@Data
@AllArgsConstructor
public class ContactDto {

    private int contactId;
    @NotEmpty
    private String firstName;
    @NotEmpty
    private String lastName;
    @ValidPhoneNumber
    private String mobilePhone;
    @NotEmpty
    private Set<ContactGroupDto> groups = new HashSet<>();

    public ContactDto() {
    }

    public ContactDto(int contactId, String mobilePhone) {
        this.contactId = contactId;
        this.mobilePhone = mobilePhone;
    }

    public ContactDto(int contactId, String firstName, String lastName, String mobilePhone) {
        this.contactId = contactId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.mobilePhone = mobilePhone;
    }

    public String getFullName() {
        return firstName + " " + lastName;
    }

}
