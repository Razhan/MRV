package com.bilibili.following.prvcompiler.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

public class StringUtils {

    public static final Pattern PATTERN_STARTS_WITH_SET = Pattern.compile("set[A-Z]\\w*");

    public static String toUpperCamelCase(String str) {
        if (str == null || str.length() <= 0) {
            return "";
        }

        List<Character> separators = new ArrayList<>(Arrays.asList('_', ' '));

        StringBuilder stringBuilder = new StringBuilder(str.length());

        for (int i = 0; i < str.length(); i++) {
            Character previous = getOrNull(str, i - 1);
            Character current = str.charAt(i);

            if (!separators.contains(current)) {
                if (previous == null || separators.contains(previous)) {
                    stringBuilder.append(Character.toUpperCase(current));
                } else {
                    stringBuilder.append(current);
                }
            }
        }

        return stringBuilder.toString();
    }

    public static String removeSetPrefix(String string) {
        if (!PATTERN_STARTS_WITH_SET.matcher(string).matches()) {
            return string;
        }

        return String.valueOf(string.charAt(3)).toLowerCase() + string.substring(4);
    }

    public static String getIdListString(List<Integer> ids) {
        if (ids == null || ids.isEmpty()) {
            return "";
        }

        StringBuilder str = new StringBuilder();
        for (int id : ids) {
            str.append(String.valueOf(id)).append(", ");
        }

        str.delete(str.length() - 2, str.length());

        return str.toString();
    }

    private static Character getOrNull(String str, int index) {
        if (str == null || str.length() <= 0) {
            return null;
        }

        if (index >= 0 && index < str.length()) {
            return str.charAt(index);
        } else {
            return null;
        }
    }

}
