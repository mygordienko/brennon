package com.mygordienko.brennon.ticket;

import com.mygordienko.brennon.core.common.BrennonDomainEvent;

public record TicketAttachmentAddedEvent(TicketId ticketId, TicketAttachmentId attachmentId) implements BrennonDomainEvent {
    @Override 
    public String toString() {
        return "ticket attachment added: %s:%s".formatted(ticketId, attachmentId);
    }
}
