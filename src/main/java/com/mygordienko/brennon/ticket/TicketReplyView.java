package com.mygordienko.brennon.ticket;

import java.time.OffsetDateTime;

import com.mygordienko.brennon.core.auth.BrennonUserProfile;

import lombok.Getter;

public class TicketReplyView {
    public TicketReplyView(TicketReplyId id, TicketId ticketId, BrennonUserProfile author, String text,
            OffsetDateTime created, OffsetDateTime updated) {
        this.id = id;
        this.ticketId = ticketId;
        this.author = author;
        this.text = text;
        this.created = created;
        this.updated = updated;
    }
    @Getter
    private final TicketReplyId id;
    @Getter
    private final TicketId ticketId;
    @Getter
    private final BrennonUserProfile author;
    @Getter
    private final String text;
    @Getter
    private final OffsetDateTime created;
    @Getter
    private final OffsetDateTime updated;

}
