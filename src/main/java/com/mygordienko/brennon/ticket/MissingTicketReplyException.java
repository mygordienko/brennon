package com.mygordienko.brennon.ticket;

public class MissingTicketReplyException extends RuntimeException {
    private MissingTicketReplyException(String message) {
        super(message);
    }

    private static final String MESSAGE_TEMPLATE = "Ticket reply with id %s not found";
    
    public static MissingTicketReplyException forReplyId(TicketReplyId ticketReplyId) {
        return new MissingTicketReplyException(MESSAGE_TEMPLATE.formatted(ticketReplyId));
    }
}
