package com.mygordienko.brennon.ticket;

import java.time.OffsetDateTime;
import java.util.Comparator;
import java.util.List;

import com.mygordienko.brennon.core.auth.BrennonUserProfile;

import lombok.Getter;

public class Ticket {
    public Ticket(TicketId id, TicketStatus status, BrennonUserProfile author, String subject, String text,
            OffsetDateTime created, OffsetDateTime updated, List<TicketReply> replies) {
        this.id = id;
        this.status = status;
        this.author = author;
        this.subject = subject;
        this.text = text;
        this.created = created;
        this.updated = updated;
        this.replies = List.copyOf(replies).stream()
            .sorted(Comparator.comparing(TicketReply::getUpdated))
            .toList();
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

    private final List<TicketReply> replies;

    public List<TicketReply> getReplies() {
        return List.copyOf(replies);
    }
}
