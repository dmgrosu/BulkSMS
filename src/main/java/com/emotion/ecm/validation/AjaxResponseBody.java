package com.emotion.ecm.validation;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.validation.ObjectError;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AjaxResponseBody {

    private String message;
    private List<ObjectError> errors;

}
