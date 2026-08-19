package com.mygordienko.brennon.ticket;

import java.time.OffsetDateTime;
import java.util.List;

import com.mygordienko.brennon.core.auth.BrennonUserProfile;

import lombok.Getter;

/**
 * Rich representation of a {@link Ticket} that includes {@link BrennonUserProfile} for ticket author
 *  as well as rich representation for each reply.
 */
public class TicketDetailsView {
    public TicketDetailsView(TicketId id, TicketStatus status, BrennonUserProfile author, String subject, String text,
            OffsetDateTime created, OffsetDateTime updated, List<TicketReplyView> replies) {
        this.id = id;
        this.status = status;
        this.author = author;
        this.subject = subject;
        this.text = text;
        this.created = created;
        this.updated = updated;
        this.replies = replies;
    }

    @Getter
    private final TicketId id;
    @Getter
    private final TicketStatus status;
    @Getter
    private final BrennonUserProfile author;
    @Getter
    private final String subject;
    @Getter
    private final String text;
    @Getter
    private final OffsetDateTime created;
    @Getter
    private final OffsetDateTime updated;

    private final List<TicketReplyView> replies;

    public List<TicketReplyView> getReplies() {
        return List.copyOf(replies);
    }
}
