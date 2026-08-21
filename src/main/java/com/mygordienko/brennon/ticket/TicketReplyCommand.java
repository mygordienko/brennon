package com.mygordienko.brennon.ticket;

public record TicketReplyCommand(
    TicketId ticketId,
    String text
) { 
}
