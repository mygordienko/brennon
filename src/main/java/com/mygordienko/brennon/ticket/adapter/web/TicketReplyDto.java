package com.mygordienko.brennon.ticket.adapter.web;

import java.time.OffsetDateTime;

public record TicketReplyDto(
    String id,
    String authorId,
    String text,
    String auhtorFullName,
    OffsetDateTime created,
    OffsetDateTime updated
) {
}
