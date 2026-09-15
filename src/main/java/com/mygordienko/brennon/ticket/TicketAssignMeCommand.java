package com.mygordienko.brennon.ticket;

public record TicketAssignMeCommand(
    TicketId ticketId,
    Integer version
) {
    
}
