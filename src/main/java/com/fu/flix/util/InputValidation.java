package com.fu.flix.util;

import java.text.Normalizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class InputValidation {
    private static final String PHONE_REGEX = "^(03|05|07|08|09|01[2|6|8|9])([0-9]{8})$";
    private static final String PASSWORD_REGEX = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{6,10}$";
    private static final String EMAIL_REGEX = "^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$";
    private static final String NAME_REGEX = "^[a-zA-Z\\s]{3,}$";
    private static final String IDENTITY_CARD_NUMBER_REGEX = "^\\d{9,12}$";

    public static boolean isValidDate(String date, String pattern) {
        try {
            DateFormatUtil.getLocalDate(date, pattern);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

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

    public static boolean isEmailValid(String email, boolean isNullable) {
        if (email == null && isNullable) {
            return true;
        } else if (email == null && !isNullable) {
            return false;
        }
        Pattern pattern = Pattern.compile(EMAIL_REGEX, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }

    public static boolean isIdentityCardNumberValid(String identityCardNumber) {
        if (identityCardNumber == null) {
            return false;
        }
        Pattern pattern = Pattern.compile(IDENTITY_CARD_NUMBER_REGEX, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(identityCardNumber);
        return matcher.matches();
    }

    public static boolean isNameValid(String name, Long maxLength) {
        if (name == null) {
            return false;
        }
        if (name.length() > maxLength) {
            return false;
        }
        name = removeAccent(name);
        Pattern pattern = Pattern.compile(NAME_REGEX, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(name);
        return matcher.matches();
    }

    public static boolean isDescriptionValid(String description, Long maxLength) {
        if (description == null) {
            return true;
        }

        return description.length() < maxLength;
    }

    public static String removeAccent(String s) {
        String temp = Normalizer.normalize(s, Normalizer.Form.NFD);
        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        return pattern.matcher(temp).replaceAll("");
    }
}
