package com.mygordienko.brennon.ticket;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.mygordienko.brennon.core.auth.BrennonUserProfileService;

public class TicketService {
    public TicketService(BrennonUserProfileService userProfileService, TicketStoragePort ticketStoragePort) {
        this.userProfileService = userProfileService;
        this.ticketStoragePort = ticketStoragePort;
    }

    private final BrennonUserProfileService userProfileService;
    private final TicketStoragePort ticketStoragePort;

    public Page<Ticket> getMyTickets(Pageable pageable) {
        var currentUser = userProfileService.getCurrentUser();
        return ticketStoragePort.getTicketsByAuthor(currentUser.id(), pageable);
    }
}
