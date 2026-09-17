package com.mygordienko.brennon.core.auth;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public class BrennonUserProfileService {
    public BrennonUserProfileService(BrennonUserProfilePort userStoragePort,
        BrennonUserAccountPort userPort
    ) {
        this.userStoragePort = userStoragePort;
        this.userPort = userPort;
    }

    private final BrennonUserProfilePort userStoragePort;
    private final BrennonUserAccountPort userPort;

    public BrennonUserProfile getUser(BrennonUserId userId) {
        return userStoragePort.getById(userId).orElseThrow(() -> MissingUserException.forId(userId));
    }

    public List<BrennonUserProfile> getUsers(Collection<BrennonUserId> userIds) {
        return userStoragePort.getByIds(userIds);
    }

    public BrennonUserProfile getCurrentUser() {
        var username = userPort.getCurrentUsername()
        .orElseThrow(() -> new RuntimeException("Missing auth context"));
        return userStoragePort.getByLogin(username)
        .orElseThrow(() -> MissingUserException.forLogin(username));
    }
    
    public Optional<BrennonUserAccount> getUserAccount(BrennonUserLogin login) {
        return userPort.getUserAccount(login);
    }
}
