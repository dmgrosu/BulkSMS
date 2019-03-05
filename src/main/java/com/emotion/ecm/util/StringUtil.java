package com.emotion.ecm.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringUtil {

    /**
     * ^[A-Za-z0-9 \r\n@£$¥èéùìòÇØøÅå\u0394_\u03A6\u0393\u039B\u03A9\u03A0\u03A8\u03A3\u0398\u039EÆæßÉ!"#$%&amp;'()*+,\-./:;&lt;=&gt;?¡ÄÖÑÜ§¿äöñüà^{}\\\[~\]|\u20AC]*$
     * <p>
     * Assert position at the beginning of the string «^»
     * Match a single character present in the list below «[A-Za-z0-9 \r\n@£$¥èéùìòÇØøÅå\u0394_\u03A6\u0393\u039B\u03A9\u03A0\u03A8\u03A3\u0398\u039EÆæßÉ!"#$%&amp;'()*+,\-./:;&lt;=&gt;?¡ÄÖÑÜ§¿äöñüà^{}\\\[~\]|\u20AC]*»
     * Between zero and unlimited times, as many times as possible, giving back as needed (greedy) «*»
     * A character in the range between "A" and "Z" «A-Z»
     * A character in the range between "a" and "z" «a-z»
     * A character in the range between "0" and "9" «0-9»
     * The character " " « »
     * A carriage return character «\r»
     * A line feed character «\n»
     * One of the characters "@£$¥èéùìòÇØøÅå" «@£$¥èéùìòÇØøÅå»
     * Unicode character U+0394 «\u0394», Greek capital Delta
     * The character "_" «_»
     * Unicode character U+03A6 «\u03A6», Greek capital Phi
     * Unicode character U+0393 «\u0393», Greek capital Gamma
     * Unicode character U+039B «\u039B», Greek capital Lambda
     * Unicode character U+03A9 «\u03A9», Greek capital Omega
     * Unicode character U+03A0 «\u03A0», Greek capital Pi
     * Unicode character U+03A8 «\u03A8», Greek capital Psi
     * Unicode character U+03A3 «\u03A3», Greek capital Sigma
     * Unicode character U+0398 «\u0398», Greek capital Theta
     * Unicode character U+039E «\u039E», Greek capital Xi
     * One of the characters "ÆæßÉ!"#$%&amp;'()*+," «ÆæßÉ!"#$%&amp;'()*+,»
     * A - character «\-»
     * One of the characters "./:;&lt;=&gt;?¡ÄÖÑÜ§¿äöñüà^{}" «./:;&lt;=&gt;?¡ÄÖÑÜ§¿äöñüà^{}»
     * A \ character «\\»
     * A [ character «\[»
     * The character "~" «~»
     * A ] character «\]»
     * The character "|" «|»
     * Unicode character U+20AC «\u20AC», Euro sign
     * Assert position at the end of the string (or before the line break at the end of the string, if any) «$»
     */
    private static final String GSM_CHARACTERS_REGEX = "^[A-Za-z0-9 \\r\\n@£$¥èéùìòÇØøÅå\u0394_\u03A6\u0393\u039B\u03A9\u03A0\u03A8\u03A3\u0398\u039EÆæßÉ!\"#$%&amp;'()*+,\\-./:;&lt;=&gt;?¡ÄÖÑÜ§¿äöñüà^{}\\\\\\[~\\]|\u20AC]*$";

    public static boolean isUTF16(String initialText) {
        if (initialText == null) {
            return false;
        }
        return !initialText.matches(GSM_CHARACTERS_REGEX);
    }

    public static List<String> createSmsParts(String initialText) throws IllegalArgumentException {

        List<String> result = new ArrayList<>();

        if (initialText == null || initialText.isEmpty()) {
            throw new IllegalArgumentException("initial text null or empty");
        }

        int maxChars = 153;
        int maxSize = 160;
        if (isUTF16(initialText)) {
            maxChars = 64;
            maxSize = 70;
        }

        initialText = initialText.replaceAll("\\s", " ");

        if (initialText.length() > maxSize) {
            String[] parts = initialText.split("(?<=\\G.{" + maxChars + "})");
            result.addAll(Arrays.asList(parts));
        } else {
            result.add(initialText);
        }

        return result;
    }

    public static long convertExpirationTimeInMillis(String expTime) {
        long result = 0L;

        if (expTime == null || expTime.isEmpty()) {
            return result;
        }

        int hours = Integer.parseInt(expTime.substring(6, 8));
        int minutes = Integer.parseInt(expTime.substring(8, 10));

        result = (hours * 60 + minutes) * 60 * 1000;

        return result;
    }
}
