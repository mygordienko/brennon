package com.mygordienko.brennon.profile.adapter.rest;

import java.time.OffsetDateTime;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.mygordienko.brennon.core.auth.BrennonUserId;
import com.mygordienko.brennon.core.auth.BrennonUserLogin;
import com.mygordienko.brennon.core.common.FileId;
import com.mygordienko.brennon.profile.BrennonUserProfile;

public class ProfileImageUrlResolverTest {

    private final ProfileImageUrlResolver resolver = new ProfileImageUrlResolver();
    private final BrennonUserId profileId = BrennonUserId.of("2");
    private final BrennonUserLogin login = BrennonUserLogin.of("john@mail.com");
    private final OffsetDateTime created = OffsetDateTime.parse("2026-08-30T15:30:00+02:00");

    @Test
    @DisplayName("Composes versioned profile image URL")
    public void givenProfileWithImage_whenUrlResolved_thenVersionedRelativeUrlReturned() {
        var fileId = FileId.of("file-1");
        var profile = new BrennonUserProfile(profileId, 0, login, "John", "Smith", fileId, created, created);

        Assertions.assertThat(resolver.resolve(profile))
            .contains("/api/v1/profile/2/images/file-1");
    }

    @Test
    @DisplayName("Returns no URL when profile has no image")
    public void givenProfileWithoutImage_whenUrlResolved_thenEmptyResultReturned() {
        var profile = new BrennonUserProfile(profileId, 0, login, "John", "Smith", null, created, created);

        Assertions.assertThat(resolver.resolve(profile)).isEmpty();
    }
}
