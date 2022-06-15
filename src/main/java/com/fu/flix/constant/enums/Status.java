package com.fu.flix.constant.enums;

public enum Status {
    PENDING("PE"),
    CANCELLED("CC"),
    CONFIRMED("CF"),
    DONE("DO"),
    FIXING("FX"),
    PAYMENT_WAITING("PW");

    private final String id;

    Status(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }
}