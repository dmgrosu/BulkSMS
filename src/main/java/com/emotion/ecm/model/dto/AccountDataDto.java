package com.emotion.ecm.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.NotNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AccountDataDto {

    @NotNull
    private String name;

    @NotNull
    private MultipartFile file;

    private boolean override;
    private String fileName;
}
