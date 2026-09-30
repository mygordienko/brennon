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
    UserRelationDto currentUserRelation,
    List<TicketReplyDto> replies,
    List<TicketAttachmentDto> attachments
) {
    public static record UserInfoDto(
        String id,
        String fullName)
    {}

    public static record UserRelationDto(
        boolean canClose,
        boolean canEdit,
        boolean canReply,
        boolean canAssign
    )
    {}
}
