package com.mygordienko.brennon.ticket;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.mygordienko.brennon.core.auth.BrennonUserId;

public interface TicketStoragePort {
    Page<Ticket> getTicketsByAuthor(BrennonUserId userId, TicketRequestFilter filter, Pageable pageable);
    Optional<Ticket> getTicketById(TicketId ticketId);
    Ticket saveTicket(Ticket ticket);
}
