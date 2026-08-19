package com.mygordienko.brennon.ticket;

import java.time.OffsetDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.domain.Persistable;
import org.springframework.data.jdbc.core.mapping.AggregateReference;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import com.mygordienko.brennon.core.auth.BrennonUserId;
import com.mygordienko.brennon.core.auth.BrennonUserProfile;

import lombok.Getter;

@Table(name = "ticket_replies")
public class TicketReply implements Persistable<TicketReplyId> {
    public TicketReply(TicketReplyId id, TicketId ticketId, AggregateReference<BrennonUserProfile, BrennonUserId> author, String text,
            OffsetDateTime created, OffsetDateTime updated) {
        this.id = id;
        this.ticketId = ticketId;
        this.author = author;
        this.text = text;
        this.created = created;
        this.updated = updated;
    }

    @Id
    @Getter
    @Column("id")
    private final TicketReplyId id;
    @Getter
    @Column("ticket_id")
    private final TicketId ticketId;
    @Getter
    @Column("author_id")
    private final AggregateReference<BrennonUserProfile, BrennonUserId> author;
    @Getter
    @Column("text")
    private final String text;
    @Getter
    @Column("created")
    private final OffsetDateTime created;
    @Getter
    @Column("updated")
    private final OffsetDateTime updated;
    @Override
    public boolean isNew() {
        return this.id == null;
    }

    
}
