package com.mygordienko.brennon.core.auth;

public class BrennonUserProfileService {
    public BrennonUserProfileService(BrennonUserProfileStoragePort userStoragePort,
        CurrentUserPort currentUserPort
    ) {
        this.userStoragePort = userStoragePort;
        this.currentUserPort = currentUserPort;
    }

    private final BrennonUserProfileStoragePort userStoragePort;
    private final CurrentUserPort currentUserPort;

    public BrennonUserProfile getUser(BrennonUserId userId) {
        return userStoragePort.getById(userId).orElseThrow(() -> MissingUserException.forId(userId));
    }

    public BrennonUserProfile getCurrentUser() {
        var username = currentUserPort.getCurrentUsername()
        .orElseThrow(() -> new RuntimeException("Missing auth context"));
        return userStoragePort.getByLogin(username)
        .orElseThrow(() -> MissingUserException.forLogin(username));
    }    
}
