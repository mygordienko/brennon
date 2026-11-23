package com.mygordienko.brennon.profile;

import java.time.OffsetDateTime;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.mygordienko.brennon.core.auth.BrennonUserId;
import com.mygordienko.brennon.core.auth.BrennonUserLogin;
import com.mygordienko.brennon.core.common.FileId;

public class BrennonUserProfileTest {
    private final BrennonUserId userId = BrennonUserId.of("user-1");
    private final BrennonUserLogin login = BrennonUserLogin.of("john@mail.com");
    private final OffsetDateTime created = OffsetDateTime.parse("2026-08-30T15:30:00+02:00");
    private final OffsetDateTime updated = OffsetDateTime.parse("2026-08-30T15:35:00+02:00");

    private BrennonUserProfile newProfile(FileId profileImageFileId) {
        return new BrennonUserProfile(userId, 0, login, "John", "Smith", profileImageFileId, created, created);
    }

    @Test
    @DisplayName("Has no profile image by default")
    public void givenNewProfile_whenProfileImageQueried_thenEmptyResultReturned() {
        Assertions.assertThat(newProfile(null).getProfileImageFileId()).isEmpty();
    }

    @Test
    @DisplayName("Updates profile image file reference and timestamp")
    public void givenProfile_whenProfileImageUpdated_thenNewFileReferenceAndTimestampAreStored() {
        var fileId = FileId.of("file-1");
        var profile = newProfile(null);

        profile.updateProfileImage(userId, fileId, updated);

        Assertions.assertThat(profile.getProfileImageFileId()).contains(fileId);
        Assertions.assertThat(profile.getCreated()).isEqualTo(created);
        Assertions.assertThat(profile.getUpdated()).isEqualTo(updated);
    }

    @Test
    @DisplayName("Rejects profile image update by a different user")
    public void givenProfile_whenProfileImageUpdatedByAnotherUser_thenIllegalStateReported() {
        var profile = newProfile(null);

        Assertions.assertThatThrownBy(() -> profile.updateProfileImage(
                BrennonUserId.of("user-2"), FileId.of("file-1"), updated))
            .isInstanceOf(IllegalStateException.class);

        Assertions.assertThat(profile.getProfileImageFileId()).isEmpty();
    }

    @Test
    @DisplayName("Rejects profile image update to the current image file")
    public void givenProfile_whenSameProfileImageUpdated_thenIllegalArgumentReported() {
        var fileId = FileId.of("file-1");
        var profile = newProfile(fileId);

        Assertions.assertThatThrownBy(() -> profile.updateProfileImage(userId, fileId, updated))
            .isInstanceOf(IllegalArgumentException.class);

        Assertions.assertThat(profile.getProfileImageFileId()).contains(fileId);
    }

    @Test
    @DisplayName("Rejects missing profile image file reference")
    public void givenProfile_whenNullProfileImageUpdated_thenIllegalArgumentReported() {
        var profile = newProfile(null);

        Assertions.assertThatThrownBy(() -> profile.updateProfileImage(userId, null, updated))
            .isInstanceOf(IllegalArgumentException.class);
    }
}
