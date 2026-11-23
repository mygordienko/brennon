package com.mygordienko.brennon.profile.internal;

import java.util.Collection;
import java.util.List;

import com.mygordienko.brennon.core.auth.BrennonUserAccountApi;
import com.mygordienko.brennon.core.auth.BrennonUserId;
import com.mygordienko.brennon.core.auth.MissingUserException;
import com.mygordienko.brennon.profile.BrennonUserProfile;
import com.mygordienko.brennon.profile.BrennonUserProfileInfoApi;

public class BrennonUserProfileService implements BrennonUserProfileInfoApi {
    public BrennonUserProfileService(BrennonUserProfilePort userStoragePort,
        BrennonUserAccountApi userPort
    ) {
        this.userStoragePort = userStoragePort;
        this.userPort = userPort;
    }

    private final BrennonUserProfilePort userStoragePort;
    private final BrennonUserAccountApi userPort;

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

    public BrennonUserProfile save(BrennonUserProfile profile) {
        return userStoragePort.save(profile);
    }
}
