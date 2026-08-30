package com.mygordienko.brennon.ticket;

public record TicketEditReplyCommand(
    TicketId ticketId,
    Integer version,
    TicketReplyId replyId,
    String text
) {
    
}
