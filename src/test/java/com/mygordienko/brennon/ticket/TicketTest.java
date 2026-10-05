package com.mygordienko.brennon.ticket;

import java.time.OffsetDateTime;
import java.util.Set;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.jdbc.core.mapping.AggregateReference;

import com.mygordienko.brennon.core.auth.BrennonUserId;
import com.mygordienko.brennon.core.auth.BrennonUserProfile;
import com.mygordienko.brennon.ticket.internal.Ticket;
import com.mygordienko.brennon.ticket.internal.TicketReply;

public class TicketTest {
    @Test
    @DisplayName("Updates timestamps when a reply is added")
    public void givenExistingTicket_whenReplyAdded_thenUpdatesTicketUpdatedTimestamp() {
        var ticketId = TicketId.of(1L);
        var authorId = BrennonUserId.of("john");
        var assigneeId = BrennonUserId.of("jeff");
        var ticket = new Ticket(
            ticketId, 
            0, 
            TicketStatus.NEW, 
            AggregateReference.<BrennonUserProfile, BrennonUserId>to(authorId),
            AggregateReference.<BrennonUserProfile, BrennonUserId>to(assigneeId),
            "subject", 
            "text", 
            OffsetDateTime.parse("2026-08-30T15:30:00+02:00"), 
            OffsetDateTime.parse("2026-08-30T15:30:00+02:00"), 
            Set.of(),
            Set.of(), 0 ,0);

        var originTicketCreated = ticket.getCreated();
        var replyTime = OffsetDateTime.parse("2026-08-30T15:32:00+02:00");

        ticket.addReply(authorId, "Reply 1", replyTime);

        Assertions.assertThat(ticket.getCreated()).isEqualTo(originTicketCreated);
        Assertions.assertThat(ticket.getUpdated()).isEqualTo(replyTime);
    }

    @Test
    @DisplayName("Updates timestamps when a reply gets updated")
    public void givenExistingTicket_whenReplyUpdated_thenUpdatesTicketUpdatedTimestamp() {
        var ticketId = TicketId.of(1L);
        var ticketReplyId = TicketReplyId.of(1);
        var authorId = BrennonUserId.of("john");
        var assigneeId = BrennonUserId.of("jeff");

        var replyOrigTime = OffsetDateTime.parse("2026-08-30T15:32:00+02:00");

        var ticket = new Ticket(
            ticketId, 
            0, 
            TicketStatus.NEW, 
            AggregateReference.<BrennonUserProfile, BrennonUserId>to(authorId),
            AggregateReference.<BrennonUserProfile, BrennonUserId>to(assigneeId),
            "subject", 
            "text", 
            OffsetDateTime.parse("2026-08-30T15:30:00+02:00"), 
            OffsetDateTime.parse("2026-08-30T15:30:00+02:00"), 
            Set.of(
                new TicketReply(ticketReplyId, ticketId, AggregateReference.to(authorId), "old text", replyOrigTime, replyOrigTime)
            ),
            Set.of(), 0, 0);

        var originTicketCreated = ticket.getCreated();
        var replyUpdateTime = OffsetDateTime.parse("2026-08-30T15:35:00+02:00");
        
        ticket.editReply(authorId, ticketReplyId, "new text", replyUpdateTime);
        
        Assertions.assertThat(ticket.getCreated()).isEqualTo(originTicketCreated);
        Assertions.assertThat(ticket.getUpdated()).isEqualTo(replyUpdateTime);
    }

    @Test
    @DisplayName("Updates timestamps when assignee changes")
    public void givenExistingTicket_whenAssigneeUpdated_thenUpdatesTicketUpdatedTimestamp() {
        var ticketId = TicketId.of(1L);
        var authorId = BrennonUserId.of("john");
        var assigneeId = BrennonUserId.of("jeff");

        var ticket = new Ticket(
            ticketId, 
            0, 
            TicketStatus.NEW, 
            AggregateReference.<BrennonUserProfile, BrennonUserId>to(authorId),
            null,
            "subject", 
            "text", 
            OffsetDateTime.parse("2026-08-30T15:30:00+02:00"), 
            OffsetDateTime.parse("2026-08-30T15:30:00+02:00"), 
            Set.of(),
            Set.of(), 0, 0);

        var originTicketCreated = ticket.getCreated();
        var operationTime = OffsetDateTime.parse("2026-08-30T15:35:00+02:00");
        
        ticket.assignTo(assigneeId, operationTime);
        
        Assertions.assertThat(ticket.getCreated()).isEqualTo(originTicketCreated);
        Assertions.assertThat(ticket.getUpdated()).isEqualTo(operationTime);
        Assertions.assertThat(ticket.getAssignee()).isPresent();
        Assertions.assertThat(ticket.getAssignee().get().getId()).isEqualTo(assigneeId);
    }

    @Test
    @DisplayName("Updates status when assigned")
    public void givenNewTicket_whenAssigned_thenUpdatesTicketStatusToInProgress() {
        var ticketId = TicketId.of(1L);
        var authorId = BrennonUserId.of("john");
        var assigneeId = BrennonUserId.of("jeff");

        var ticket = new Ticket(
            ticketId, 
            0, 
            TicketStatus.NEW, 
            AggregateReference.<BrennonUserProfile, BrennonUserId>to(authorId),
            null,
            "subject", 
            "text", 
            OffsetDateTime.parse("2026-08-30T15:30:00+02:00"), 
            OffsetDateTime.parse("2026-08-30T15:30:00+02:00"), 
            Set.of(),
            Set.of(), 0, 0);

        var originTicketCreated = ticket.getCreated();
        var operationTime = OffsetDateTime.parse("2026-08-30T15:35:00+02:00");
        
        ticket.assignTo(assigneeId, operationTime);
        
        Assertions.assertThat(ticket.getCreated()).isEqualTo(originTicketCreated);
        Assertions.assertThat(ticket.getUpdated()).isEqualTo(operationTime);
        Assertions.assertThat(ticket.getStatus()).isEqualTo(TicketStatus.IN_PROGRESS);
        Assertions.assertThat(ticket.getAssignee()).isPresent();
        Assertions.assertThat(ticket.getAssignee().get().getId()).isEqualTo(assigneeId);
    }
}
