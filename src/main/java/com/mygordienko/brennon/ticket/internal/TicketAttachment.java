package com.mygordienko.brennon.ticket.internal;

import java.time.OffsetDateTime;

import org.jspecify.annotations.Nullable;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Persistable;
import org.springframework.data.jdbc.core.mapping.AggregateReference;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import com.mygordienko.brennon.core.auth.BrennonUserId;
import com.mygordienko.brennon.core.auth.BrennonUserProfile;
import com.mygordienko.brennon.ticket.FileId;
import com.mygordienko.brennon.ticket.TicketAttachmentId;
import com.mygordienko.brennon.ticket.TicketId;
import com.mygordienko.brennon.ticket.TicketReplyId;

@Table(name = "ticket_attachments")
public class TicketAttachment implements Persistable<TicketAttachmentId> {
    public TicketAttachment(TicketAttachmentId id, TicketId ticketId, TicketReplyId replyId, FileId fileId, String name, AggregateReference<BrennonUserProfile, BrennonUserId> author, OffsetDateTime created) {
        this.id = id;
        this.ticketId = ticketId;
        this.replyId = replyId;
        this.fileId = fileId;
        this.name = name;
        this.author = author;
        this.created = created;
    }

    public static TicketAttachment createNew(
        TicketAttachmentId id,
        TicketId ticketId,
        TicketReplyId replyId,
        FileId fileId,
        String name,
        BrennonUserId author,
        OffsetDateTime created
    ) {
        var attachment = new TicketAttachment(id, ticketId, replyId, fileId, name, AggregateReference.to(author), created);
        attachment.explicitNew = true;
        return attachment;
    }

    @Id
    @Column("id")
    private final TicketAttachmentId id;
    @Override
    public @Nullable TicketAttachmentId getId() {
        return this.id;
    }

    @Column("ticket_id")
    private final TicketId ticketId;
    public TicketId getTicketId() {
        return this.ticketId;
    }

    @Column("reply_id")
    private final TicketReplyId replyId;
    public TicketReplyId getTicketReplyId() {
        return this.replyId;
    }

    @Column("file_id")
    private final FileId fileId;
    public FileId getFileId() {
        return this.fileId;
    }

    @Column("name")
    private final String name;
    public String getName() {
        return this.name;
    }

    @Column("author_id")
    private final AggregateReference<BrennonUserProfile, BrennonUserId> author;
    public BrennonUserId getAuthorId() {
        return this.author.getId();
    }

    @Column("created")
    private final OffsetDateTime created;
    public OffsetDateTime getCreated() {
        return this.created;
    }

    @Transient
    private boolean explicitNew;

    @Override
    public boolean isNew() {
        return this.explicitNew;
    }
    
}
