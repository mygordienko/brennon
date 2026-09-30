package com.mygordienko.brennon.ticket;

/**
 * Thrown when attachment was not found by provided parameters and context.
 */
public class MissingTicketAttachmentException extends RuntimeException {
    private MissingTicketAttachmentException(String message) {
        super(message);
    }

    private static final String MESSAGE_TEMPLATE = "Ticket %s does not have attachment with id %s";
    
    public static MissingTicketAttachmentException forTicket(TicketId ticketId, TicketAttachmentId attachmentId) {
        return new MissingTicketAttachmentException(MESSAGE_TEMPLATE.formatted(ticketId, attachmentId));
    }
}
