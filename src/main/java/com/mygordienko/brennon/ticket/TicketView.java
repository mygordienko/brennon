package com.mygordienko.brennon.ticket;

import java.time.OffsetDateTime;

import com.mygordienko.brennon.core.auth.BrennonUserProfile;

import lombok.Getter;

/**
 * Short version, supposed to be used in tables/lists. Replies are not loaded.
 */
public class TicketView {
    public TicketView(
            TicketId id, 
            Integer version, 
            TicketStatus status, 
            BrennonUserProfile author, 
            BrennonUserProfile assignee, 
            String subject, 
            String text,
            OffsetDateTime created, 
            OffsetDateTime updated) {
        this.id = id;
        this.version = version;
        this.status = status;
        this.author = author;
        this.assignee = assignee;
        this.subject = subject;
        this.text = text;
        this.created = created;
        this.updated = updated;
    }

    @Getter
    private final TicketId id;
    @Getter
    private final Integer version;
    @Getter
    private final TicketStatus status;
    @Getter
    private final BrennonUserProfile author;
    @Getter
    private final BrennonUserProfile assignee;
    @Getter
    private final String subject;
    @Getter
    private final String text;
    @Getter
    private final OffsetDateTime created;
    @Getter
    private final OffsetDateTime updated;
}
