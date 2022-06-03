package com.fpt.flix.flix_app.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class InputValidation {
    private static final String PHONE_REGEX = "^(0[3|5|7|8|9]){1}([0-9]{8,9})$";
    private static final String PASSWORD_REGEX = "^(\\S){6,10}$";

    public static boolean isPhoneValid(String phoneNumber) {
        Pattern pattern = Pattern.compile(PHONE_REGEX);
        Matcher matcher = pattern.matcher(phoneNumber);
        return matcher.matches();
    }

    public static boolean isPasswordValid(String password) {
        Pattern pattern = Pattern.compile(PASSWORD_REGEX);
        Matcher matcher = pattern.matcher(password);
        return matcher.matches();
    }
}