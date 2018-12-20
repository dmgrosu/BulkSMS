package com.emotion.ecm.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class StringUtil {

    public static List<String> createSmsParts(String initialText, boolean isEncoded) throws IllegalArgumentException {

        List<String> result = new ArrayList<>();

        if (initialText == null || initialText.isEmpty()) {
            throw new IllegalArgumentException("initial text null or empty");
        }

        int maxChars = 153;
        int maxSize = 160;
        if (isEncoded) {
            maxChars = 64;
            maxSize = 70;
        }

        initialText = initialText.replaceAll("\\s", "");

        if (initialText.length() > maxSize) {
            String[] parts = initialText.split("(?<=\\G.{" + maxChars + "})");
            result.addAll(Arrays.asList(parts));
        } else {
            result.add(initialText);
        }

        return result;
    }
}
