package com.mygordienko.brennon.ticket;

import lombok.EqualsAndHashCode;

@EqualsAndHashCode(of = {"value"})
public class TicketId {
    private TicketId(Long value) {
        this.value = value;
    }

    public static final TicketId of(Long value) {
        return new TicketId(value);
    }

    public static final TicketId of(int value) {
        return new TicketId(Long.valueOf(value));
    }

    private final Long value;

    @Override
    public String toString() {
        return String.valueOf(value);
    }

    public Long getValue() {
        return value;
    }    
}
