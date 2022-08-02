package com.fu.flix.util;

import java.text.DecimalFormat;

public class DataFormatter {
    public static String getVietNamePhoneNumber(String phone) {
        return "+84" + phone.substring(1);
    }

    public static String getVietnamMoneyFormatted(Long money) {
        DecimalFormat formatter = new DecimalFormat("#,###.00");
        return formatter.format(money) + " VNĐ";
    }
}
