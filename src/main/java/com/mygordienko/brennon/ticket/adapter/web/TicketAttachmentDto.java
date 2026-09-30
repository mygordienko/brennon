package com.mygordienko.brennon.ticket.adapter.web;

import java.time.OffsetDateTime;

public record TicketAttachmentDto(
    String id,
    String authorId,
    String replyId,
    String name,
    OffsetDateTime created,
    UserRelationDto currentUserRelation
) {
    public static record UserRelationDto(
        boolean isAuthor
    )
    {}
}
