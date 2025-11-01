package org.springframework.util;

public final class StringUtils {
    private StringUtils() {
    }

    public static boolean hasText(String value) {
        if (value == null) {
            return false;
        }
        for (int i = 0; i < value.length(); i++) {
            if (!Character.isWhitespace(value.charAt(i))) {
                return true;
            }
        }
        return false;
    }
}
