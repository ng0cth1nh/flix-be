package com.fu.flix.constant.enums;

public enum FeedbackStatus {
    PROCESSING("PR"),
    REJECTED("RJ"),
    DONE("DO"),
    PENDING("PE");

    private final String id;

    FeedbackStatus(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }
}
