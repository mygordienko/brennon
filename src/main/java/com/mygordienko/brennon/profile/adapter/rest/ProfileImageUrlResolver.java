package com.mygordienko.brennon.profile.adapter.rest;

import java.util.Optional;

import com.mygordienko.brennon.profile.BrennonUserProfile;

/**
 * Composes a relative profile image download URL for a profile.
 *
 * NOTE: the image file ID is part of the URL, so the URL changes whenever the profile image is updated.
 */
public class ProfileImageUrlResolver {
    private static final String URL_TEMPLATE = "/api/v1/profile/%s/images/%s";

    public Optional<String> resolve(BrennonUserProfile profile) {
        return profile.getProfileImageFileId()
                .map(fileId -> URL_TEMPLATE.formatted(profile.getId(), fileId));
    }
}
