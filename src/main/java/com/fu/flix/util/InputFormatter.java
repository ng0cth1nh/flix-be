package com.fu.flix.util;

public class InputFormatter {
    public static String getString(String str) {
        if (str != null) {
            str = str.trim();
        }
        return str;
    }
}
