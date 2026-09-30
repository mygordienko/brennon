package com.mygordienko.brennon.ticket;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import com.mygordienko.brennon.core.auth.BrennonUserProfile;
import com.mygordienko.brennon.ticket.internal.Ticket;

import lombok.Getter;

/**
 * Rich representation of a {@link Ticket} that includes {@link BrennonUserProfile} for ticket author
 *  as well as rich representation for each reply.
 */
public class TicketDetailsView {
    public TicketDetailsView(
            TicketId id, 
            Integer version, 
            TicketStatus status, 
            BrennonUserProfile author,
            BrennonUserProfile assignee, 
            String subject, 
            String text,
            OffsetDateTime created, 
            OffsetDateTime updated,
            UserRelation userRelation,
            List<TicketReplyView> replies,
            List<TicketAttachmentView> attachments) {
        this.id = id;
        this.version = version;
        this.status = status;
        this.author = author;
        this.assignee = assignee;
        this.subject = subject;
        this.text = text;
        this.created = created;
        this.updated = updated;
        this.userRelation = userRelation;
        this.replies = List.copyOf(replies);  
        this.attachments = List.copyOf(attachments);
    }

    @Getter
    private final TicketId id;
    @Getter
    private final Integer version;
    @Getter
    private final TicketStatus status;
    @Getter
    private final BrennonUserProfile author;
    private final BrennonUserProfile assignee;
    public Optional<BrennonUserProfile> getAssignee() {
        return Optional.ofNullable(assignee);
    }

    @Getter
    private final String subject;
    @Getter
    private final String text;
    @Getter
    private final OffsetDateTime created;
    @Getter
    private final OffsetDateTime updated;
    @Getter
    private final UserRelation userRelation;

    private final List<TicketReplyView> replies;

    public List<TicketReplyView> getReplies() {
        return List.copyOf(replies);
    }

    private final List<TicketAttachmentView> attachments;

    public List<TicketAttachmentView> getAttachments() {
        return List.copyOf(attachments);
    }

    /**
     * What current user can do with this ticket.
     */
    public static record UserRelation(
        boolean canClose,
        boolean canEdit,
        boolean canReply,
        boolean canAssign
    ) {
    }
}
