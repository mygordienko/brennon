package com.mygordienko.brennon.ticket.adapter.web;

import java.time.OffsetDateTime;

public record TicketRecordDto(
    String id,
    int version,
    String status,
    String subject,
    String auhtorFullName,
    OffsetDateTime created,
    OffsetDateTime updated
) {
    
}
