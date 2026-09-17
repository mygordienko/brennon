package com.mygordienko.brennon.ticket.internal;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.mygordienko.brennon.core.auth.BrennonUserId;
import com.mygordienko.brennon.ticket.TicketId;
import com.mygordienko.brennon.ticket.TicketRequestFilter;

public interface TicketStoragePort {
    Page<Ticket> getTicketsByAuthor(BrennonUserId userId, TicketRequestFilter filter, Pageable pageable);
    Page<Ticket> getTicketsByAssignee(BrennonUserId userId, TicketRequestFilter filter, Pageable pageable);
    Optional<Ticket> getTicketById(TicketId ticketId);
    Ticket saveTicket(Ticket ticket);
}
