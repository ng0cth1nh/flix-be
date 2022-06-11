package com.fu.flix.constant.enums;

public enum Status {
    PENDING("P"), CANCELLED("C"), CONFIRMED("CF");

    private final String id;

    Status(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }
}
