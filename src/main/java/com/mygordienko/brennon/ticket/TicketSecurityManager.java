package com.mygordienko.brennon.ticket;

import org.jspecify.annotations.NonNull;

import com.mygordienko.brennon.core.auth.BrennonUserProfile;
import com.mygordienko.brennon.core.auth.BrennonUserProfileService;
import com.mygordienko.brennon.core.auth.MissingUserException;

public class TicketSecurityManager {
    public TicketSecurityManager(BrennonUserProfileService userProfileService) {
        this.userProfileService = userProfileService;
    }

    private final BrennonUserProfileService userProfileService;

    public boolean userCanRead(@NonNull BrennonUserProfile currentUser, @NonNull Ticket ticket) {
        var currentUserAccount = userProfileService.getUserAccount(currentUser.getLogin()).orElseThrow(() -> MissingUserException.forLogin(currentUser.getLogin()));
        return currentUser.getId().equals(ticket.getAuthor().getId()) || currentUserAccount.isSupport();
    }

    public boolean userCanReply(@NonNull BrennonUserProfile currentUser, @NonNull Ticket ticket) {
        var currentUserAccount = userProfileService.getUserAccount(currentUser.getLogin()).orElseThrow(() -> MissingUserException.forLogin(currentUser.getLogin()));
        return currentUser.getId().equals(ticket.getAuthor().getId()) || currentUserAccount.isSupport();
    }

    public boolean userCanClose(@NonNull BrennonUserProfile currentUser, @NonNull Ticket ticket) {
        return currentUser.getId().equals(ticket.getAuthor().getId());
    }

    public boolean userCanEdit(@NonNull BrennonUserProfile currentUser, @NonNull Ticket ticket) {
        return currentUser.getId().equals(ticket.getAuthor().getId());
    }

    public boolean userCanAssign(@NonNull BrennonUserProfile currentUser, @NonNull Ticket ticket) {
        var currentUserAccount = userProfileService.getUserAccount(currentUser.getLogin()).orElseThrow(() -> MissingUserException.forLogin(currentUser.getLogin()));
        return !currentUser.getId().equals(ticket.getAssignee().map(p -> p.getId()).orElse(null)) && currentUserAccount.isSupport();
    }
}
