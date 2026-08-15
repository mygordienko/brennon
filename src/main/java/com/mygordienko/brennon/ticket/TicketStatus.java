package com.mygordienko.brennon.ticket;

import java.util.Arrays;

public enum TicketStatus {
    NEW(10, "New"),
    IN_PROGRESS(20, "In Progress"),
    CLOSED(30, "Closed");

    private TicketStatus(int code, String name) {
        this.code = code;
        this.name = name;
    }

    private final int code;
    private final String name;

    public String getDisplayName() {
        return this.name;
    }

    public static TicketStatus of(int code) {
        return Arrays.stream(TicketStatus.values())
        .filter(value -> value.code == code)
        .findAny()
        .orElseThrow(() -> new IllegalArgumentException("Invalid ticket status code"));
    }
}
