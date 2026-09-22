package com.mygordienko.brennon.ticket;

import java.time.OffsetDateTime;

import com.mygordienko.brennon.core.common.BrennonDomainEvent;

public record TicketClosedEvent(TicketId ticketId, OffsetDateTime createdAt, OffsetDateTime closedAt) implements BrennonDomainEvent {
    @Override 
    public String toString() {
        return "ticket closed: %s:%s".formatted(ticketId, closedAt);
    }    
}
