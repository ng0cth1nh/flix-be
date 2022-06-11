package com.fu.flix.constant.enums;

public enum PaymentMethod {
    CASH("C"),
    VNPay("V");
    private final String id;

    PaymentMethod(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }
}
