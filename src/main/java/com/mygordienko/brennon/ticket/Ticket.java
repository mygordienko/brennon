package com.mygordienko.brennon.ticket;

import java.time.OffsetDateTime;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.domain.AbstractAggregateRoot;
import org.springframework.data.domain.Persistable;
import org.springframework.data.jdbc.core.mapping.AggregateReference;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.MappedCollection;
import org.springframework.data.relational.core.mapping.Table;

import com.mygordienko.brennon.core.auth.BrennonUserId;
import com.mygordienko.brennon.core.auth.BrennonUserProfile;

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
            Set<TicketReply> replies) {
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
    }

    public static Ticket createNew(
        TicketStatus status, 
        BrennonUserId author,
        BrennonUserId assignee,
        String subject, 
        String text,
        OffsetDateTime created, 
        OffsetDateTime updated) {
            var authorRef = AggregateReference.<BrennonUserProfile, BrennonUserId>to(author);
            var assigneeRef = (assignee == null) ? null : AggregateReference.<BrennonUserProfile, BrennonUserId>to(assignee);
            return new Ticket(null, null, status, authorRef, assigneeRef, subject, text,
            created, updated, new HashSet<>());
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
    private final AggregateReference<BrennonUserProfile, BrennonUserId> assignee;
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

    @Override
    public boolean isNew() {
        return this.id == null;
    }

    public void addReply(BrennonUserId authorId, String text, OffsetDateTime created) {
        if (this.status == TicketStatus.CLOSED) {
            throw new IllegalStateException("Replying on closed ticket");
        }

        if (!this.author.getId().equals(authorId)) {
            // TODO: support reply
            throw new IllegalStateException("Not an author's reply");
        }

        var reply = new TicketReply(null, this.id, AggregateReference.to(authorId), text, created, created);

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

    public void editReply(BrennonUserId authorId, TicketReplyId replyId, String newText, OffsetDateTime updated) {
        if (this.status == TicketStatus.CLOSED) {
            throw new IllegalStateException("Editing closed ticket");
        }

        if (!this.author.getId().equals(authorId)) {
            // TODO: support reply
            throw new IllegalStateException("Not an author's reply");
        }

        var reply = this.replies.stream()
            .filter(r -> r.getId().equals(replyId))
            .findFirst()
            .orElseThrow(() -> MissingTicketReplyException.forReplyId(replyId));

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
    }
}
