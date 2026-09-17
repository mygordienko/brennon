package com.mygordienko.brennon.core.auth.internal;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import com.mygordienko.brennon.core.auth.BrennonUserAccount;
import com.mygordienko.brennon.core.auth.BrennonUserId;
import com.mygordienko.brennon.core.auth.BrennonUserInfoApi;
import com.mygordienko.brennon.core.auth.BrennonUserLogin;
import com.mygordienko.brennon.core.auth.BrennonUserProfile;
import com.mygordienko.brennon.core.auth.MissingUserException;

public class BrennonUserProfileService implements BrennonUserInfoApi {
    public BrennonUserProfileService(BrennonUserProfilePort userStoragePort,
        BrennonUserAccountPort userPort
    ) {
        this.userStoragePort = userStoragePort;
        this.userPort = userPort;
    }

    private final BrennonUserProfilePort userStoragePort;
    private final BrennonUserAccountPort userPort;

    @Override
    public BrennonUserProfile getUser(BrennonUserId userId) {
        return userStoragePort.getById(userId).orElseThrow(() -> MissingUserException.forId(userId));
    }

    @Override
    public List<BrennonUserProfile> getUsers(Collection<BrennonUserId> userIds) {
        return userStoragePort.getByIds(userIds);
    }

    @Override
    public BrennonUserProfile getCurrentUser() {
        var username = userPort.getCurrentUsername()
        .orElseThrow(() -> new RuntimeException("Missing auth context"));
        return userStoragePort.getByLogin(username)
        .orElseThrow(() -> MissingUserException.forLogin(username));
    }
    
    @Override
    public Optional<BrennonUserAccount> getUserAccount(BrennonUserLogin login) {
        return userPort.getUserAccount(login);
    }
}
