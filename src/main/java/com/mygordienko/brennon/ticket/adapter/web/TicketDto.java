package com.mygordienko.brennon.ticket.adapter.web;

import java.time.OffsetDateTime;
import java.util.List;

public record TicketDto(
    String id,
    Integer version,
    String authorId,
    String status,
    String subject,
    String text,
    String auhtorFullName,
    OffsetDateTime created,
    OffsetDateTime updated,
    List<TicketReplyDto> replies
) {
    public TicketDto(int id) {
        // Must delegate to the canonical constructor
        this(String.valueOf(id), null, null, null, null, null, null, null, null, null); 
    }
}
