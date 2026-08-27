package com.mygordienko.brennon.ticket;

public record TicketEditCommand(
    TicketId ticketId,
    Integer version,
    String subject,
    String text
) {
    
}
