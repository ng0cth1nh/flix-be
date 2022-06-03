package com.fu.flix.constant.enums;

public enum RedisDataTypeEnum {
    UNDEFINED("a"),
    NULL("b"),
    BOOLEAN("0"),
    STRING("1"),
    NUMBER("2"),
    DATE("3"),
    OBJECT("4");

    private String type;

    private RedisDataTypeEnum(String type) {
        this.type = type;
    }

    public String getType() {
        return this.type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
