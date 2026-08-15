package com.mygordienko.brennon.ticket;

import lombok.EqualsAndHashCode;

@EqualsAndHashCode(of = {"value"})
public class TicketId {
    private TicketId(String value) {
        this.value = value;
    }

    public static final TicketId of(String value) {
        return new TicketId(value);
    }

    private final String value;

    @Override
    public String toString() {
        return value;
    }    
}
