package com.mygordienko.brennon.ticket;

import lombok.EqualsAndHashCode;

@EqualsAndHashCode(of = {"value"})
public class TicketReplyId {
    private TicketReplyId(String value) {
        this.value = value;
    }

    public static final TicketReplyId of(String value) {
        return new TicketReplyId(value);
    }

    private final String value;

    @Override
    public String toString() {
        return value;
    }
}