package com.emotion.ecm.validation;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.validation.FieldError;

import java.util.List;

@Data
@AllArgsConstructor
public class AjaxResponseBody {

    private boolean valid;
    private List<FieldError> errors;

}
