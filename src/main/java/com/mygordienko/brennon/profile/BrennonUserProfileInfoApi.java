package com.mygordienko.brennon.profile;

import java.util.Collection;
import java.util.List;

import com.mygordienko.brennon.core.auth.BrennonUserId;
import com.mygordienko.brennon.profile.internal.BrennonUserProfileService;

/**
 * User info API this module exposes.
 * This API is supposed to be used by other modules instead of directly accessing {@link BrennonUserProfileService}.
 */
public interface BrennonUserProfileInfoApi {
    public BrennonUserProfile getUser(BrennonUserId userId);
    public List<BrennonUserProfile> getUsers(Collection<BrennonUserId> userIds);
    public BrennonUserProfile getCurrentUser();
}
