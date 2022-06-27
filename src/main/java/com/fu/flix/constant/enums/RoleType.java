package com.fu.flix.constant.enums;

public enum RoleType {
    ROLE_ADMIN("A"),
    ROLE_CUSTOMER("C"),
    ROLE_REPAIRER("R"),
    ROLE_PENDING_REPAIRER("PR"),
    ROLE_MANAGER("M"),
    ROLE_STAFF("S");

    private final String id;

    RoleType(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }
}
