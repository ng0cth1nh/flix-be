package com.fu.flix.util;

import org.apache.logging.log4j.util.Strings;

import java.util.Date;

public class RandomUtil {
    public static String generateCode() {
        Date date = new Date();
        String[] alphabet = {"A", "B", "C", "D", "E", "F", "G", "H", "I", "J",
                "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "W", "Z"};
        int prefixLength = 4;
        String prefix = Strings.EMPTY;

        for (int i = 0; i < prefixLength; i++) {
            int alphaIndex = (int) (Math.random() * alphabet.length);
            prefix += alphabet[alphaIndex];
        }

        return prefix + date.getTime();
    }
}
