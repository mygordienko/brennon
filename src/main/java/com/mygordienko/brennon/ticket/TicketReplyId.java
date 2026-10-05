package com.mygordienko.brennon.ticket;

import lombok.EqualsAndHashCode;

@EqualsAndHashCode(of = {"value"})
public class TicketReplyId {
    private TicketReplyId(Integer value) {
        this.value = value;
    }

    public static final TicketReplyId of(Integer value) {
        return new TicketReplyId(value);
    }
    public static final TicketReplyId of(int value) {
        return new TicketReplyId(Integer.valueOf(value));
    }

    private final Integer value;

    @Override
    public String toString() {
        return String.valueOf(value);
    }

    public Integer getValue() {
        return value;
    } 
}