package com.mygordienko.brennon.ticket.internal;

import org.jspecify.annotations.NonNull;

import com.mygordienko.brennon.core.auth.BrennonUserInfoApi;
import com.mygordienko.brennon.core.auth.BrennonUserProfile;
import com.mygordienko.brennon.core.auth.MissingUserException;

public class TicketSecurityManager {
    public TicketSecurityManager(BrennonUserInfoApi userService) {
        this.userService = userService;
    }

    private final BrennonUserInfoApi userService;

    public boolean userCanRead(@NonNull BrennonUserProfile currentUser, @NonNull Ticket ticket) {
        var currentUserAccount = userService.getUserAccount(currentUser.getLogin()).orElseThrow(() -> MissingUserException.forLogin(currentUser.getLogin()));
        return currentUser.getId().equals(ticket.getAuthor().getId()) || currentUserAccount.isSupport();
    }

    public boolean userCanReply(@NonNull BrennonUserProfile currentUser, @NonNull Ticket ticket) {
        var currentUserAccount = userService.getUserAccount(currentUser.getLogin()).orElseThrow(() -> MissingUserException.forLogin(currentUser.getLogin()));
        return currentUser.getId().equals(ticket.getAuthor().getId()) || currentUserAccount.isSupport();
    }

    public boolean userCanClose(@NonNull BrennonUserProfile currentUser, @NonNull Ticket ticket) {
        return currentUser.getId().equals(ticket.getAuthor().getId());
    }

    public boolean userCanEdit(@NonNull BrennonUserProfile currentUser, @NonNull Ticket ticket) {
        return currentUser.getId().equals(ticket.getAuthor().getId());
    }

    public boolean userCanAssign(@NonNull BrennonUserProfile currentUser, @NonNull Ticket ticket) {
        var currentUserAccount = userService.getUserAccount(currentUser.getLogin()).orElseThrow(() -> MissingUserException.forLogin(currentUser.getLogin()));
        return !currentUser.getId().equals(ticket.getAssignee().map(p -> p.getId()).orElse(null)) && currentUserAccount.isSupport();
    }
}
