package com.emotion.ecm.validation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CustomEmailValidator implements ConstraintValidator<ValidEmail, String> {

    @Value("${ecm.regExpPatterns.email}")
    private String patternStr;

    private static final String DEFAULT_PATTERN = "^[_A-Za-z0-9-+](.[_A-Za-z0-9-]+)*@[A-Za-z0-9-](.[A-Za-z0-9]+)*(.[A-Za-z]{2,})$";
    private static final Logger LOGGER = LoggerFactory.getLogger(CustomEmailValidator.class);

    @Override
    public void initialize(ValidEmail constraintAnnotation) {
    }

    @Override
    public boolean isValid(String email, ConstraintValidatorContext context) {
        return (validateEmail(email));
    }

    private boolean validateEmail(String email) {
        Pattern pattern;
        try {
            pattern = Pattern.compile(patternStr);
        } catch (Exception e) {
            LOGGER.warn(e.getMessage());
            pattern = Pattern.compile(DEFAULT_PATTERN);
        }
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }

}
