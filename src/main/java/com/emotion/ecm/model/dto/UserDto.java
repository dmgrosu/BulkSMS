package com.emotion.ecm.model.dto;

import com.emotion.ecm.enums.UserStatus;
import com.emotion.ecm.validation.ValidEmail;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Data
@AllArgsConstructor
public class UserDto {

    private int userId;

    @NotEmpty
    private String firstName;
    @NotEmpty
    private String lastName;
    @ValidEmail
    private String email;
    @NotEmpty
    private String username;
    @NotEmpty
    private String password;
    @NotEmpty
    private String confirmPassword;
    @Enumerated(value = EnumType.STRING)
    private UserStatus status;

    private int accountId;
    private String accountName;

    public UserDto() {
    }

    public UserDto(int userId, int accountId) {
        this.userId = userId;
        this.accountId = accountId;
    }

    public UserDto(int id, String firstName, String lastName, String email,
                   String username, UserStatus status, int accountId, String accountName) {
        this.userId = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.username = username;
        this.status = status;
        this.accountId = accountId;
        this.accountName = accountName;
    }

}
