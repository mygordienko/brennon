package com.mygordienko.brennon.profile;

import org.springframework.dao.OptimisticLockingFailureException;

import com.mygordienko.brennon.core.auth.BrennonUserId;

/**
 * This exception is used in addition to {@link OptimisticLockingFailureException}.
 *
 * Profile updates are done by loading the aggregate, applying the change and saving it back.
 * The aggregate is loaded with the latest version from the database, so the version sent by the client
 * must be checked explicitly before applying the change.
 *
 * Database-level check is still needed and will be done by Spring, potentially throwing `OptimisticLockingFailureException`.
 */
public class ProfileConcurrentModificationException extends RuntimeException {
    private ProfileConcurrentModificationException(String message) {
        super(message);
    }

    private static final String MESSAGE_TEMPLATE = "Concurrent update detected for profile %s";

    public static ProfileConcurrentModificationException forProfile(BrennonUserId profileId) {
        return new ProfileConcurrentModificationException(MESSAGE_TEMPLATE.formatted(profileId));
    }
}
