package com.mygordienko.brennon.ticket;

import java.util.Collection;

public record TicketEditCommand(
    TicketId ticketId,
    Integer version,
    String subject,
    String text,
    Collection<FileId> attachmentsToAdd,
    Collection<TicketAttachmentId> attachmentsToRemove
) {
    
}
