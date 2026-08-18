package com.mygordienko.brennon.ticket;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;

import com.mygordienko.brennon.core.auth.BrennonUserProfileService;

public class TicketService {
    public TicketService(
        BrennonUserProfileService userProfileService, 
        TicketStoragePort ticketStoragePort,
        TicketSecurityManager ticketSecurityManager) {
        this.userProfileService = userProfileService;
        this.ticketStoragePort = ticketStoragePort;
        this.ticketSecurityManager = ticketSecurityManager;
    }

    private final BrennonUserProfileService userProfileService;
    private final TicketStoragePort ticketStoragePort;
    private final TicketSecurityManager ticketSecurityManager;

    @PreAuthorize("hasAuthority('user:read')")
    public Page<Ticket> getMyTickets(Pageable pageable) {
        var currentUser = userProfileService.getCurrentUser();
        return ticketStoragePort.getTicketsByAuthor(currentUser.id(), pageable);
    }

    @PreAuthorize("hasAuthority('user:read')")
    public Ticket getTicket(TicketId ticketId) {
        var currentUser = userProfileService.getCurrentUser();
        return ticketStoragePort.getTicketById(ticketId)
            // TODO: use AOP
            .filter(ticket -> ticketSecurityManager.userHasAccess(currentUser, ticket))
            .orElseThrow(() -> MissingTicketException.forTicket(ticketId));
    }
}
