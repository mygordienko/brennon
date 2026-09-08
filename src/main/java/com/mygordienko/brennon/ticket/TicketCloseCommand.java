package com.mygordienko.brennon.ticket;

public record TicketCloseCommand(
    TicketId ticketId,
    Integer version
) {
}
