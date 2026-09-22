package com.mygordienko.brennon.ticket.adapter.metrics;

public enum TicketMetrics {
    TICKETS_CREATED("brennon.tickets.created", "Total number of tickets created"),
    TICKET_RESOLUTION_TIME("brennon.tickets.resolution.time", "Time taken to resolve a ticket");

    private TicketMetrics(String code, String description) {
        this.code = code;
        this.description = description;
    }

    private final String code;
    private final String description;

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }
}
