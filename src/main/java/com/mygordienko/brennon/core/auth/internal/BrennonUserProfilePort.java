package com.mygordienko.brennon.core.auth.internal;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import com.mygordienko.brennon.core.auth.BrennonUserId;
import com.mygordienko.brennon.core.auth.BrennonUserLogin;
import com.mygordienko.brennon.core.auth.BrennonUserProfile;

/**
 * Abstraction to load {@link BrennonUserProfile} from storage.
 */
public interface BrennonUserProfilePort {
    Optional<BrennonUserProfile> getById(BrennonUserId userId);
    /**
     * Selects aggregates by provided IDs
     * @param userId IDs to search for, duplicates ignored.
     * @return Selected Aggregates, ordered by ID ASC.
     */
    List<BrennonUserProfile> getByIds(Collection<BrennonUserId> userIds);
    Optional<BrennonUserProfile> getByLogin(BrennonUserLogin login);
}
