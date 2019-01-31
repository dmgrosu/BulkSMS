package com.emotion.ecm.validation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CustomPhoneValidator implements ConstraintValidator<ValidPhoneNumber, String> {

    @Value("${ecm.regExpPatterns.phoneNumber}")
    private String patternStr;

    private static final String DEFAULT_PATTERN = "\\d{10,13}";
    private static final Logger LOGGER = LoggerFactory.getLogger(CustomPhoneValidator.class);

    @Override
    public void initialize(ValidPhoneNumber constraintAnnotation) {
    }

    @Override
    public boolean isValid(String email, ConstraintValidatorContext context) {
        return (validatePhoneNumber(email));
    }

    private boolean validatePhoneNumber(String phoneNumber) {
        Pattern pattern;
        try {
            pattern = Pattern.compile(patternStr);
        } catch (Exception e) {
            LOGGER.warn(e.getMessage());
            pattern = Pattern.compile(DEFAULT_PATTERN);
        }
        Matcher matcher = pattern.matcher(phoneNumber);
        return matcher.matches();
    }

}
