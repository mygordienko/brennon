package com.mygordienko.brennon.profile.internal;

import java.io.InputStream;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;

import com.mygordienko.brennon.core.auth.BrennonUserId;
import com.mygordienko.brennon.core.auth.MissingUserException;
import com.mygordienko.brennon.core.common.ClockManager;
import com.mygordienko.brennon.core.common.FileId;
import com.mygordienko.brennon.core.common.ResourceBundle;
import com.mygordienko.brennon.profile.ProfileConcurrentModificationException;
import com.mygordienko.brennon.profile.ProfileImageStorageException;
import com.mygordienko.brennon.profile.ProfileImageUpdatedEvent;

public class ProfileImageService {
    public ProfileImageService(
            ProfileImageStoragePort profileImageStoragePort,
            BrennonUserProfileService userProfileService,
            ClockManager clockManager) {
        this.profileImageStoragePort = profileImageStoragePort;
        this.userProfileService = userProfileService;
        this.clockManager = clockManager;
    }

    private static final Logger log = LoggerFactory.getLogger("com.mygordienko.brennon.profile.internal.ProfileImageService");

    private final ProfileImageStoragePort profileImageStoragePort;
    private final BrennonUserProfileService userProfileService;
    private final ClockManager clockManager;

    @PreAuthorize("hasAuthority('user:write')")
    public FileId generateNextFileId() {
        return FileId.of(UUID.randomUUID().toString());
    }

    @PreAuthorize("hasAuthority('user:write')")
    public void saveTemp(FileId fileId, InputStream is, String fileName) {
        var currentUser = userProfileService.getCurrentUser();
        profileImageStoragePort.saveTemp(currentUser.getId(), fileId, is, fileName);
    }

    @PreAuthorize("hasAuthority('user:write')")
    @Transactional
    public void saveProfileImage(FileId fileId, Integer version) {
        var currentUser = userProfileService.getCurrentUser();

        if (!Objects.equals(currentUser.getVersion(), version)) {
            throw ProfileConcurrentModificationException.forProfile(currentUser.getId());
        }

        // NOTE: the file is moved before the aggregate is saved, so a failed save leaves the previous image intact
        profileImageStoragePort.moveToFinal(currentUser.getId(), fileId);

        var updated = OffsetDateTime.ofInstant(clockManager.now(), ZoneId.systemDefault());
        currentUser.updateProfileImage(currentUser.getId(), fileId, updated);
        userProfileService.save(currentUser);
    }

    /**
     * Evicts the previously referenced image file.
     *
     * NOTE: best-effort, a storage failure must not fail the profile update that has already been persisted.
     */
    @EventListener
    public void processProfileImageUpdated(ProfileImageUpdatedEvent event) {
        log.debug("processProfileImageUpdated profile: {} previous file: {} new file: {}",
                event.profileId(), event.previousFileId(), event.newFileId());

        if (event.previousFileId() == null) {
            return;
        }

        try {
            profileImageStoragePort.delete(event.profileId(), event.previousFileId());
        } catch (ProfileImageStorageException e) {
            log.warn("Could not delete previous profile image {} of profile {}",
                    event.previousFileId(), event.profileId(), e);
        }
    }

    @PreAuthorize("hasAuthority('user:read')")
    public boolean currentUserHasProfileImage() {
        var currentUser = userProfileService.getCurrentUser();
        return currentUser.getProfileImageFileId().isPresent();
    }

    @PreAuthorize("hasAuthority('user:read')")
    public Optional<ResourceBundle> getCurrentUserProfileImage() {
        var currentUser = userProfileService.getCurrentUser();
        return currentUser.getProfileImageFileId()
                .flatMap(fileId -> profileImageStoragePort.getImageResource(currentUser.getId(), fileId));
    }

    @PreAuthorize("hasAuthority('user:read')")
    public Optional<ResourceBundle> getProfileImage(BrennonUserId profileId) {
        try {
            var profile = userProfileService.getUser(profileId);
            return profile.getProfileImageFileId()
                    .flatMap(fileId -> profileImageStoragePort.getImageResource(profileId, fileId));
        } catch (MissingUserException e) {
            return Optional.empty();
        }
    }
}
