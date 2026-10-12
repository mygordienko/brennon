package com.mygordienko.brennon.ticket;

import java.time.OffsetDateTime;

import com.mygordienko.brennon.core.auth.BrennonUserProfile;

public class TicketRecordView {
    public TicketRecordView(
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

    private final TicketId id;
    public TicketId getId() {
        return id;
    }
    
    private final Integer version;
    public Integer getVersion() {
        return version;
    }
    
    private final TicketStatus status;
    public TicketStatus getStatus() {
        return status;
    }
    
    private final BrennonUserProfile author;
    public BrennonUserProfile getAuthor() {
        return author;
    }
    
    private final BrennonUserProfile assignee;
    public BrennonUserProfile getAssignee() {
        return assignee;
    }
    
    private final String subject;
    public String getSubject() {
        return subject;
    }
    
    private final String text;
    public String getText() {
        return text;
    }
    
    private final OffsetDateTime created;
    public OffsetDateTime getCreated() {
        return created;
    }

    private final OffsetDateTime updated;
    public OffsetDateTime getUpdated() {
        return updated;
    }
}