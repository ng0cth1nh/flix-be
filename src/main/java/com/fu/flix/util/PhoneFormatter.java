package com.fu.flix.util;

public class PhoneFormatter {
    public static String getVietNamePhoneNumber(String phone) {
        return "+84" + phone.substring(1);
    }
}
