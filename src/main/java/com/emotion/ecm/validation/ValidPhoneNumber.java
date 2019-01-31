package com.emotion.ecm.validation;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;

@Target({TYPE, FIELD, ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = CustomPhoneValidator.class)
@Documented
public @interface ValidPhoneNumber {

    String message() default "{ecm.messages.phoneValidationError}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}