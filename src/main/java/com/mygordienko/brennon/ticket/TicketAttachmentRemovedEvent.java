package com.mygordienko.brennon.ticket;

import com.mygordienko.brennon.core.common.BrennonDomainEvent;

public record TicketAttachmentRemovedEvent(TicketId ticketId, TicketAttachmentId attachmentId, FileId fileId) implements BrennonDomainEvent {
    @Override 
    public String toString() {
        return "ticket attachment removed: %s:%s:%s".formatted(ticketId, attachmentId, fileId);
    }
}
