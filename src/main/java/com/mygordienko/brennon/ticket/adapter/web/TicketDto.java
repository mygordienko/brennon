package com.mygordienko.brennon.ticket.adapter.web;

import java.time.OffsetDateTime;
import java.util.List;

public record TicketDto(
    String id,
    Integer version,
    UserInfoDto author,
    UserInfoDto assignee,
    String status,
    String statusCode,
    String subject,
    String text,
    OffsetDateTime created,
    OffsetDateTime updated,
    List<TicketReplyDto> replies
) {
    public TicketDto(int id) {
        // Must delegate to the canonical constructor
        this(String.valueOf(id), null, null, null, null, null, null, null, null, null, null); 
    }

    public TicketDto(long id) {
        // Must delegate to the canonical constructor
        this(String.valueOf(id), null, null, null, null, null, null, null, null, null, null); 
    }

    public static record UserInfoDto(
        String id,
        String fullName)
    {}
}
