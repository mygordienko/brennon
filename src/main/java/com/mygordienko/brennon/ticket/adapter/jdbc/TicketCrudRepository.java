package com.mygordienko.brennon.ticket.adapter.jdbc;

import org.springframework.data.repository.CrudRepository;

import com.mygordienko.brennon.ticket.TicketId;
import com.mygordienko.brennon.ticket.internal.Ticket;

public interface TicketCrudRepository extends CrudRepository<Ticket, TicketId> {
    
}
