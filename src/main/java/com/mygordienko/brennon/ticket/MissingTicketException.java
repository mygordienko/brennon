package com.mygordienko.brennon.ticket;

/**
 * Thrown when ticket was not found by provided parameters and context.
 */
public class MissingTicketException extends RuntimeException {
    private MissingTicketException(String message) {
        super(message);
    }

    private static final String MESSAGE_TEMPLATE = "Ticket with number %s not found";
    
    public static MissingTicketException forTicket(TicketId ticketId) {
        return new MissingTicketException(MESSAGE_TEMPLATE.formatted(ticketId));
    }
}
