package com.emotion.ecm.util;

import org.springframework.util.StringUtils;

import java.text.ParseException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DateUtil {

    private final static DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");

    public static String formatDate(LocalDateTime dateTime) throws IllegalArgumentException {
        if (dateTime == null) {
            throw new IllegalArgumentException("input date is null");
        }
        return dateTime.format(formatter);
    }

    public static LocalDateTime parseDate(String dateString) throws ParseException {
        if (StringUtils.isEmpty(dateString)) {
            throw new ParseException("input string is null or empty", 1);
        }
        return LocalDateTime.parse(dateString, formatter);
    }

}
