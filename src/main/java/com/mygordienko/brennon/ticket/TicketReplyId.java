package com.mygordienko.brennon.ticket;

import lombok.EqualsAndHashCode;

@EqualsAndHashCode(of = {"value"})
public class TicketReplyId {
    private TicketReplyId(Long value) {
        this.value = value;
    }

    public static final TicketReplyId of(Long value) {
        return new TicketReplyId(value);
    }
    public static final TicketReplyId of(int value) {
        return new TicketReplyId(Long.valueOf(value));
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