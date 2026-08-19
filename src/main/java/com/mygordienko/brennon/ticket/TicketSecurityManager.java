package com.mygordienko.brennon.ticket;

import org.jspecify.annotations.NonNull;

import com.mygordienko.brennon.core.auth.BrennonUserProfile;

public class TicketSecurityManager {

    public TicketSecurityManager() {
    }

    public boolean userHasAccess(@NonNull BrennonUserProfile currentUser, @NonNull Ticket ticket) {
        return currentUser.getId().equals(ticket.getAuthor().getId());
    }
}
