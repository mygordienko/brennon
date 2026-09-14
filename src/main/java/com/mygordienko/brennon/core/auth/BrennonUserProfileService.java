package com.mygordienko.brennon.core.auth;

import java.util.Collection;
import java.util.List;

public class BrennonUserProfileService {
    public BrennonUserProfileService(BrennonUserProfileStoragePort userStoragePort,
        BrennonUserPort currentUserPort
    ) {
        this.userStoragePort = userStoragePort;
        this.currentUserPort = currentUserPort;
    }

    private final BrennonUserProfileStoragePort userStoragePort;
    private final BrennonUserPort currentUserPort;

    public BrennonUserProfile getUser(BrennonUserId userId) {
        return userStoragePort.getById(userId).orElseThrow(() -> MissingUserException.forId(userId));
    }

    public List<BrennonUserProfile> getUsers(Collection<BrennonUserId> userIds) {
        return userStoragePort.getByIds(userIds);
    }

    public BrennonUserProfile getCurrentUser() {
        var username = currentUserPort.getCurrentUsername()
        .orElseThrow(() -> new RuntimeException("Missing auth context"));
        return userStoragePort.getByLogin(username)
        .orElseThrow(() -> MissingUserException.forLogin(username));
    }    
}
