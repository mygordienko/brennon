package com.mygordienko.brennon.ticket.internal;

import java.time.OffsetDateTime;
import java.util.Optional;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import com.mygordienko.brennon.core.auth.BrennonUserId;
import com.mygordienko.brennon.ticket.TicketId;
import com.mygordienko.brennon.ticket.TicketStatus;

/**
 * Reduced version of {@link Ticket}, supposed to be used in tables/lists. Child entities are not loaded.
 */
@Table(name = "tickets")
public class TicketRecord {
    public TicketRecord(
            TicketId id, 
            Integer version, 
            TicketStatus status, 
            BrennonUserId authorId, 
            BrennonUserId assigneeId, 
            String subject, 
            String text,
            OffsetDateTime created, 
            OffsetDateTime updated) {
        this.id = id;
        this.version = version;
        this.status = status;
        this.authorId = authorId;
        this.assigneeId = assigneeId;
        this.subject = subject;
        this.text = text;
        this.created = created;
        this.updated = updated;
    }

    @Id
    @Column("id")
    private final TicketId id;
    public TicketId getId() {
        return id;
    }
    
    @Version
    @Column("version")
    private final Integer version;
    public Integer getVersion() {
        return version;
    }
    
    @Column("status")
    private final TicketStatus status;
    public TicketStatus getStatus() {
        return status;
    }
    
    @Column("author_id")
    private final BrennonUserId authorId;
    public BrennonUserId getAuthorId() {
        return authorId;
    }
    
    @Column("assignee_id")
    private final BrennonUserId assigneeId;
    public Optional<BrennonUserId> getAssigneeId() {
        return Optional.ofNullable(assigneeId);
    }
    
    @Column("subject")
    private final String subject;
    public String getSubject() {
        return subject;
    }
    
    @Column("text")
    private final String text;
    public String getText() {
        return text;
    }
    
    @Column("created")
    private final OffsetDateTime created;
    public OffsetDateTime getCreated() {
        return created;
    }
    
    @Column("updated")
    private final OffsetDateTime updated;
    public OffsetDateTime getUpdated() {
        return updated;
    }
}
