package com.mygordienko.brennon.ticket;

public record TicketCreateCommand(
    String subject,
    String text
) {
    
}
