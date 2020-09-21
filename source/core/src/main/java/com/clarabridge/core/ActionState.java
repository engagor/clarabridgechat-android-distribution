package com.clarabridge.core;

public enum ActionState {

    /**
     * When an buy action has been offered to the user.
     */
    OFFERED("offered"),

    /**
     * When a buy action has been paid by the user.
     */
    PAID("paid"),

    ;

    private String value;

    ActionState(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
