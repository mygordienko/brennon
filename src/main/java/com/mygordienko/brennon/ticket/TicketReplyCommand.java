package com.mygordienko.brennon.ticket;

public record TicketReplyCommand(
    TicketId ticketId,
    Integer version,
    String text
) { 
}
