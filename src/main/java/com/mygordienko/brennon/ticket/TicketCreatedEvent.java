package com.mygordienko.brennon.ticket;

import java.time.OffsetDateTime;

import com.mygordienko.brennon.core.common.BrennonDomainEvent;

public record TicketCreatedEvent(TicketId ticketId, OffsetDateTime createdAt) implements BrennonDomainEvent {
    @Override 
    public String toString() {
        return "ticket created: %s:%s".formatted(ticketId, createdAt);
    }
}