package com.fu.flix.util;

import org.apache.logging.log4j.util.Strings;

public class RandomUtil {
    public static String generateCode() {
        String[] alphabet = {"A", "B", "C", "D", "E", "F", "G", "H", "I", "J",
                "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "W", "Z",
                "1", "2", "3", "4", "5", "6", "7", "8", "9", "0"};
        int codeLength = 12;
        String code = Strings.EMPTY;

        for (int i = 0; i < codeLength; i++) {
            int alphaIndex = (int) (Math.random() * alphabet.length);
            code += alphabet[alphaIndex];
        }

        return code;
    }
}
