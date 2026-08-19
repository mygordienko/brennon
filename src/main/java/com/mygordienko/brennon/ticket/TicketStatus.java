package com.mygordienko.brennon.ticket;

import java.util.Arrays;

public enum TicketStatus {
    NEW("0010", "New"),
    IN_PROGRESS("0020", "In Progress"),
    CLOSED("0030", "Closed");

    private TicketStatus(String code, String name) {
        this.code = code;
        this.name = name;
    }

    private final String code;
    private final String name;

    public String getDisplayName() {
        return this.name;
    }

    public String getCode() {
        return this.code;
    }

    public static TicketStatus of(String code) {
        return Arrays.stream(TicketStatus.values())
        .filter(value -> value.code.equals(code))
        .findAny()
        .orElseThrow(() -> new IllegalArgumentException("Invalid ticket status code"));
    }
}
