package com.mygordienko.brennon.ticket;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;

import com.mygordienko.brennon.core.auth.BrennonUserProfileService;

public class TicketService {
    public TicketService(BrennonUserProfileService userProfileService, TicketStoragePort ticketStoragePort) {
        this.userProfileService = userProfileService;
        this.ticketStoragePort = ticketStoragePort;
    }

    private final BrennonUserProfileService userProfileService;
    private final TicketStoragePort ticketStoragePort;

    @PreAuthorize("hasAuthority('user:read')")
    public Page<Ticket> getMyTickets(Pageable pageable) {
        var currentUser = userProfileService.getCurrentUser();
        return ticketStoragePort.getTicketsByAuthor(currentUser.id(), pageable);
    }

    @PreAuthorize("hasAuthority('user:read')")
    public Ticket getTicket(TicketId ticketId) {
        var currentUser = userProfileService.getCurrentUser();

        var ti = ticketStoragePort.getTicketById(ticketId).orElse(null);

        var eq = currentUser.id().equals(ti.getAuthor().id());

        IO.println(eq);

        return ticketStoragePort.getTicketById(ticketId)
            // TODO: use AOP
            .filter(ticket -> ticket.getAuthor().id().equals(currentUser.id()))
            .orElseThrow(() -> MissingTicketException.forTicket(ticketId));
    }
}
