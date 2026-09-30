package com.mygordienko.brennon.ticket;

import java.time.OffsetDateTime;
import java.util.Optional;

import com.mygordienko.brennon.core.auth.BrennonUserId;

public class TicketAttachmentView {
    public TicketAttachmentView(TicketAttachmentId id, TicketId ticketId, BrennonUserId authorId, TicketReplyId replyId, String name,
            OffsetDateTime created, UserRelation userRelation) {
        this.id = id;
        this.ticketId = ticketId;
        this.authorId = authorId;
        this.replyId = replyId;
        this.name = name;
        this.created = created;
        this.userRelation = userRelation;
    }

    private final TicketAttachmentId id;
    public TicketAttachmentId getId() {
        return id;
    }

    private final TicketId ticketId;
    public TicketId getTicketId() {
        return ticketId;
    }
    
    private final BrennonUserId authorId;
    public BrennonUserId getAuthorId() {
        return authorId;
    }

    private final TicketReplyId replyId;
    public Optional<TicketReplyId> getReplyId() {
        return Optional.ofNullable(replyId);
    }
    
    private final String name;
    public String getName() {
        return name;
    }
    
    private final OffsetDateTime created;
    public OffsetDateTime getCreated() {
        return created;
    }
    
    private final UserRelation userRelation;
    public UserRelation getUserRelation() {
        return userRelation;
    }

    public static record UserRelation(
        boolean isAuthor
    ) {
    }
}
