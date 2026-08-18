package com.mygordienko.brennon.ticket;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;

import com.mygordienko.brennon.core.auth.BrennonUserProfileService;
import com.mygordienko.brennon.core.common.ClockManager;

public class TicketService {
    public TicketService(
        BrennonUserProfileService userProfileService, 
        TicketStoragePort ticketStoragePort,
        TicketSecurityManager ticketSecurityManager,
        ClockManager clockManager) {
        this.userProfileService = userProfileService;
        this.ticketStoragePort = ticketStoragePort;
        this.ticketSecurityManager = ticketSecurityManager;
        this.clockManager = clockManager;
    }

    private final BrennonUserProfileService userProfileService;
    private final TicketStoragePort ticketStoragePort;
    private final TicketSecurityManager ticketSecurityManager;
    private final ClockManager clockManager;

    @PreAuthorize("hasAuthority('user:read')")
    @Transactional(readOnly = true)
    public Page<Ticket> getMyTickets(Pageable pageable) {
        var currentUser = userProfileService.getCurrentUser();
        return ticketStoragePort.getTicketsByAuthor(currentUser.id(), pageable);
    }

    @PreAuthorize("hasAuthority('user:read')")
    @Transactional(readOnly = true)
    public Ticket getTicket(TicketId ticketId) {
        var currentUser = userProfileService.getCurrentUser();
        return ticketStoragePort.getTicketById(ticketId)
            // TODO: use AOP
            .filter(ticket -> ticketSecurityManager.userHasAccess(currentUser, ticket))
            .orElseThrow(() -> MissingTicketException.forTicket(ticketId));
    }

    @PreAuthorize("hasAuthority('user:write')")
    @Transactional
    public Ticket createTicket(TicketCreateRequest request) {
        var created = OffsetDateTime.ofInstant(clockManager.now(), ZoneId.systemDefault());
        var currentUser = userProfileService.getCurrentUser();
        var id = ticketStoragePort.getNextTicketId();

        var ticket = new Ticket(id, TicketStatus.NEW, currentUser, request.subject(), request.text(), created, created, List.of());

        return ticketStoragePort.saveTicket(ticket);
    }
}
