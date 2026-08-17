package com.mygordienko.brennon.ticket;

import com.mygordienko.brennon.core.auth.BrennonUserProfileService;

public class TicketSecurityManager {
    
    public TicketSecurityManager(BrennonUserProfileService userProfileService) {
        this.userProfileService = userProfileService;
    }

    private final BrennonUserProfileService userProfileService;

    public void filterForCurrentUser(Ticket ticket) {
        var currentUser = userProfileService.getCurrentUser();

        if (ticket.getAuthor().id().equals(currentUser.id())) {
            return;
        }
    }
}
