package com.mygordienko.brennon.ticket;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.mygordienko.brennon.core.auth.BrennonUserId;

public interface TicketStoragePort {
    Page<Ticket> getTicketsByAuthor(BrennonUserId userId, Pageable pageable);
}
