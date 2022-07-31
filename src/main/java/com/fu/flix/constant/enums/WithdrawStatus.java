package com.fu.flix.constant.enums;

public enum WithdrawStatus {
    SUCCESS("SS"),
    FAIL("FA"),
    PENDING("PE");

    private final String id;

    WithdrawStatus(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }
}
