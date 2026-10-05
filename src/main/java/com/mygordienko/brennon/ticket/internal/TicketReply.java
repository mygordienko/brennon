package com.mygordienko.brennon.ticket.internal;

import java.time.OffsetDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Persistable;
import org.springframework.data.jdbc.core.mapping.AggregateReference;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import com.mygordienko.brennon.core.auth.BrennonUserId;
import com.mygordienko.brennon.core.auth.BrennonUserProfile;
import com.mygordienko.brennon.ticket.TicketId;
import com.mygordienko.brennon.ticket.TicketReplyId;

import lombok.Getter;
import lombok.Setter;

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

    public static TicketReply createNew(
        TicketReplyId id,
        TicketId ticketId,
        BrennonUserId author,
        String text,
        OffsetDateTime created,
        OffsetDateTime updated
    ) {
        var reply = new TicketReply(id, ticketId, AggregateReference.to(author), text, created, updated);
        reply.explicitNew = true;
        return reply;
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
    @Setter
    @Column("text")
    private String text;
    @Getter
    @Column("created")
    private final OffsetDateTime created;
    @Getter
    @Setter
    @Column("updated")
    private OffsetDateTime updated;

    @Transient
    private boolean explicitNew;

    @Override
    public boolean isNew() {
        return this.explicitNew;
    }
}
