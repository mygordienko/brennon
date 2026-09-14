package com.mygordienko.brennon.ticket.adapter.web;

import java.time.OffsetDateTime;

public record TicketReplyDto(
    String id,
    String authorId,
    String text,
    String authorFullName,
    OffsetDateTime created,
    OffsetDateTime updated,
    UserRelationDto currentUserRelation
) {
    public static record UserRelationDto(
        boolean isAuthor
    )
    {}
}
