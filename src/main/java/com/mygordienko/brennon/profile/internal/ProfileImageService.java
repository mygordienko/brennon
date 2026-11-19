package com.mygordienko.brennon.profile.internal;

import java.io.InputStream;
import java.util.Optional;
import java.util.UUID;

import org.springframework.security.access.prepost.PreAuthorize;

import com.mygordienko.brennon.core.common.FileId;
import com.mygordienko.brennon.core.common.ResourceBundle;

public class ProfileImageService {
    public ProfileImageService(
            ProfileImageStoragePort profileImageStoragePort,
            BrennonUserProfileService userProfileService) {
        this.profileImageStoragePort = profileImageStoragePort;
        this.userProfileService = userProfileService;
    }

    private final ProfileImageStoragePort profileImageStoragePort;
    private final BrennonUserProfileService userProfileService;

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
    public void saveProfileImage(FileId fileId) {
        var currentUser = userProfileService.getCurrentUser();
        profileImageStoragePort.moveToFinal(currentUser.getId(), fileId);
    }

    @PreAuthorize("hasAuthority('user:read')")
    public boolean currentUserHasProfileImage() {
        var currentUser = userProfileService.getCurrentUser();
        return profileImageStoragePort.exists(currentUser.getId());
    }

    @PreAuthorize("hasAuthority('user:read')")
    public Optional<ResourceBundle> getCurrentUserProfileImage() {
        var currentUser = userProfileService.getCurrentUser();
        return profileImageStoragePort.getImageResource(currentUser.getId());
    }
}
