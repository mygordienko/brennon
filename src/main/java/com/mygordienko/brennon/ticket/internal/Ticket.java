package com.mygordienko.brennon.ticket.internal;

import java.time.OffsetDateTime;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.annotation.Version;
import org.springframework.data.domain.AbstractAggregateRoot;
import org.springframework.data.domain.Persistable;
import org.springframework.data.jdbc.core.mapping.AggregateReference;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.MappedCollection;
import org.springframework.data.relational.core.mapping.Table;

import com.mygordienko.brennon.core.auth.BrennonUserId;
import com.mygordienko.brennon.core.auth.BrennonUserProfile;
import com.mygordienko.brennon.ticket.FileId;
import com.mygordienko.brennon.ticket.MissingTicketReplyException;
import com.mygordienko.brennon.ticket.TicketAttachmentAddedEvent;
import com.mygordienko.brennon.ticket.TicketAttachmentId;
import com.mygordienko.brennon.ticket.TicketAttachmentRemovedEvent;
import com.mygordienko.brennon.ticket.TicketClosedEvent;
import com.mygordienko.brennon.ticket.TicketCreatedEvent;
import com.mygordienko.brennon.ticket.TicketId;
import com.mygordienko.brennon.ticket.TicketReplyId;
import com.mygordienko.brennon.ticket.TicketStatus;

import lombok.Getter;

/**
 * Ticket aggegate. 
 * Holds invariants that belong to enforced by ticket.
 */
@Table(name = "tickets")
public class Ticket extends AbstractAggregateRoot<Ticket> implements Persistable<TicketId> {
    public Ticket(
            TicketId id, 
            Integer version, 
            TicketStatus status, 
            AggregateReference<BrennonUserProfile, BrennonUserId> author, 
            AggregateReference<BrennonUserProfile, BrennonUserId> assignee,
            String subject, 
            String text,
            OffsetDateTime created,
            OffsetDateTime updated,
            Set<TicketReply> replies,
            Set<TicketAttachment> attachments) {
        this.id = id;
        this.version = version;
        this.status = status;
        this.author = author;
        this.assignee = assignee;
        this.subject = subject;
        this.text = text;
        this.created = created;
        this.updated = updated;
        this.replies = Set.copyOf(replies).stream()
            .collect(Collectors.toCollection(() -> new HashSet<>()));
        this.attachments = Set.copyOf(attachments).stream()
            .collect(Collectors.toCollection(() -> new HashSet<>()));
    }

    public static Ticket createNew(
        TicketId ticketId,
        TicketStatus status, 
        BrennonUserId author,
        BrennonUserId assignee,
        String subject, 
        String text,
        OffsetDateTime created, 
        OffsetDateTime updated) {
            var authorRef = AggregateReference.<BrennonUserProfile, BrennonUserId>to(author);
            var assigneeRef = (assignee == null) ? null : AggregateReference.<BrennonUserProfile, BrennonUserId>to(assignee);
            Ticket ticket = new Ticket(ticketId, null, status, authorRef, assigneeRef, subject, text,
            created, updated, new HashSet<>(), new HashSet<>());

            ticket.explicitNew = true;
            ticket.registerEvent(new TicketCreatedEvent(ticketId, created));

            return ticket;
    }

    @Id
    @Getter
    @Column("id")
    private final TicketId id;
    @Getter
    @Column("status")
    private TicketStatus status;
    @Getter
    @Column("author_id")
    private final AggregateReference<BrennonUserProfile, BrennonUserId> author;
    @Column("assignee_id")
    private AggregateReference<BrennonUserProfile, BrennonUserId> assignee;
    public Optional<AggregateReference<BrennonUserProfile, BrennonUserId>> getAssignee() {
        return Optional.ofNullable(assignee);
    }
    
    @Getter
    @Column("subject")
    private String subject;
    @Getter
    @Column("text")
    private String text;
    @Getter
    @Column("created")
    private final OffsetDateTime created;
    @Getter
    @Column("updated")
    private OffsetDateTime updated;
    @Getter
    @Version
    @Column("version")
    private final Integer version;

    @MappedCollection(idColumn = "ticket_id")
    private final Set<TicketReply> replies;

    public List<TicketReply> getReplies() {
        return replies.stream()
            .sorted(Comparator.comparing(TicketReply::getCreated))
            .toList();
    }

    @MappedCollection(idColumn = "ticket_id")
    private final Set<TicketAttachment> attachments;

    public List<TicketAttachment> getAttachments() {
        return attachments.stream()
            .sorted(Comparator.comparing(TicketAttachment::getCreated))
            .toList();
    }

    @Column("last_attachment_id")
    private long lastAttachmentId;

    /**
     * Explicit additional flag to mark entityt as a new one. Used when explicit ID is provided.
     */
    @Transient
    private boolean explicitNew;
    
    @Override
    public boolean isNew() {
        return this.id == null || explicitNew;
    }

    public void addReply(BrennonUserId userId, String text, OffsetDateTime created) {
        if (this.status == TicketStatus.CLOSED) {
            throw new IllegalStateException("Replying on closed ticket");
        }

        if (!this.author.getId().equals(userId) 
                && (this.assignee != null && !this.assignee.getId().equals(userId))) {
            throw new IllegalStateException("User can not reply ticket %s".formatted(this.id));
        }

        var reply = new TicketReply(null, this.id, AggregateReference.to(userId), text, created, created);

        this.replies.add(reply);
        this.updated = created;
    }

    public void editDetails(BrennonUserId authorId, String subject, String text, OffsetDateTime updated) {
        if (this.status == TicketStatus.CLOSED) {
            throw new IllegalStateException("Editing closed ticket");
        }

        if (!this.author.getId().equals(authorId)) {
            throw new IllegalStateException("Not an author's edit");
        }

        this.subject = subject;
        this.text = text;
    }

    public void editReply(BrennonUserId userId, TicketReplyId replyId, String newText, OffsetDateTime updated) {
        if (this.status == TicketStatus.CLOSED) {
            throw new IllegalStateException("Editing closed ticket");
        }

        var reply = this.replies.stream()
            .filter(r -> r.getId().equals(replyId))
            .findFirst()
            .orElseThrow(() -> MissingTicketReplyException.forReplyId(replyId));

        if (!reply.getAuthor().getId().equals(userId)) {
            throw new IllegalStateException("Not an author's reply");
        }

        reply.setText(newText);
        reply.setUpdated(updated);
        this.updated = updated;
    }

    public void closeTicket(BrennonUserId authorId, OffsetDateTime updated) {
        if (this.status == TicketStatus.CLOSED) {
            throw new IllegalStateException("Ticket is already closed");
        }

        if (!this.author.getId().equals(authorId)) {
            // TODO: close by support
            throw new IllegalStateException("Only the author can close");
        }

        this.status = TicketStatus.CLOSED;
        this.updated = updated;
        this.registerEvent(new TicketClosedEvent(id, created, updated));
    }

    public void assignTo(BrennonUserId userId, OffsetDateTime updated) {
        if (this.status == TicketStatus.CLOSED) {
            throw new IllegalStateException("Ticket is already closed");
        }

        this.assignee = AggregateReference.to(userId);

        this.status = TicketStatus.IN_PROGRESS;
        this.updated = updated;
    }

    public void addAttachment(BrennonUserId userId, FileId fileId, String name, OffsetDateTime created) {
        if (this.status == TicketStatus.CLOSED) {
            throw new IllegalStateException("Adding attachment to a closed ticket");
        }

        if (!this.author.getId().equals(userId) 
                && (this.assignee != null && !this.assignee.getId().equals(userId))) {
            throw new IllegalStateException("User %s can not add attachment to ticket %s".formatted(userId, this.id));
        }

        var attachmentId = getNextAttachmentId();
        var attachment = TicketAttachment.createNew(attachmentId, this.id, null, fileId, name, userId, created);

        this.attachments.add(attachment);
        this.updated = created;
        this.registerEvent(new TicketAttachmentAddedEvent(id, attachmentId));
    }

    public boolean removeAttachment(BrennonUserId userId, TicketAttachmentId attachmentId, OffsetDateTime updated) {
        if (this.status == TicketStatus.CLOSED) {
            throw new IllegalStateException("Removing attachment from a closed ticket");
        }

        if (!this.author.getId().equals(userId) 
                && (this.assignee != null && !this.assignee.getId().equals(userId))) {
            throw new IllegalStateException("User %s can not remove attachment from ticket %s".formatted(userId, this.id));
        }

        var attachmentToRemove = this.attachments.stream()
            .filter(att -> att.getId().equals(attachmentId))
            .findFirst();

        if (attachmentToRemove.isPresent()) {
            this.attachments.removeIf(att -> att.getId().equals(attachmentToRemove.get().getId()));
            this.registerEvent(new TicketAttachmentRemovedEvent(id, attachmentToRemove.get().getId(), attachmentToRemove.get().getFileId()));
            this.updated = updated;
            return true;
        }

        return false;
    }

    private final TicketAttachmentId getNextAttachmentId() {
        this.lastAttachmentId++;
        return TicketAttachmentId.of(this.lastAttachmentId);
    }
}
