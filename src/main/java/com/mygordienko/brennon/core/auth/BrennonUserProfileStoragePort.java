package com.mygordienko.brennon.core.auth;

import java.util.Collection;
import java.util.Optional;

/**
 * Abstraction to load {@link BrennonUserProfile} from storage.
 */
public interface BrennonUserProfileStoragePort {
    Optional<BrennonUserProfile> getById(BrennonUserId userId);
    Optional<BrennonUserProfile> getByLogin(BrennonUserLogin login);
    Collection<BrennonUserProfile> getUsers();
}
