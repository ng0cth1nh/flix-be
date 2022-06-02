package com.fpt.flix.flix_app.utils;

public class PhoneFormatter {
    public static String getVietNamePhoneNumber(String phone) {
        return "+84" + phone.substring(1);
    }
}
