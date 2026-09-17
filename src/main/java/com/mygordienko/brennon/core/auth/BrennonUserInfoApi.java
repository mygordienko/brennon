package com.mygordienko.brennon.core.auth;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import com.mygordienko.brennon.core.auth.internal.BrennonUserProfileService;

/**
 * User info API this module exposes.
 * This API is supposed to be used by other modules instead of directly accessing {@link BrennonUserProfileService}.
 */
public interface BrennonUserInfoApi {
    public BrennonUserProfile getUser(BrennonUserId userId);

    public List<BrennonUserProfile> getUsers(Collection<BrennonUserId> userIds);

    public BrennonUserProfile getCurrentUser();
    
    public Optional<BrennonUserAccount> getUserAccount(BrennonUserLogin login);
}
