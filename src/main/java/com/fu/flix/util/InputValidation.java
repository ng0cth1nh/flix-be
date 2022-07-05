package com.fu.flix.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class InputValidation {
    private static final String PHONE_REGEX = "^(03|05|07|08|09|01[2|6|8|9])([0-9]{8})$";
    private static final String PASSWORD_REGEX = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{6,10}$";
    private static final String EMAIL_REGEX = "^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$";
    private static final String FULL_NAME = "^[a-zA-Z\\s]{3,}$";

    public static boolean isPhoneValid(String phoneNumber) {
        if (phoneNumber == null) {
            return false;
        }
        Pattern pattern = Pattern.compile(PHONE_REGEX);
        Matcher matcher = pattern.matcher(phoneNumber);
        return matcher.matches();
    }

    public static boolean isPasswordValid(String password) {
        if (password == null) {
            return false;
        }
        Pattern pattern = Pattern.compile(PASSWORD_REGEX);
        Matcher matcher = pattern.matcher(password);
        return matcher.matches();
    }

    public static boolean isEmailValid(String email) {
        if (email == null) {
            return false;
        }
        Pattern pattern = Pattern.compile(EMAIL_REGEX, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }

    public static boolean isFullNameValid(String fullName) {
        if (fullName == null) {
            return false;
        }
        Pattern pattern = Pattern.compile(FULL_NAME, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(fullName);
        return matcher.matches();
    }
}
