package com.mygordienko.brennon.profile;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.mygordienko.brennon.core.auth.BrennonUserId;
import com.mygordienko.brennon.core.auth.BrennonUserLogin;
import com.mygordienko.brennon.core.common.FileId;

public class BrennonUserProfileTest {
    private final BrennonUserId userId = BrennonUserId.of("user-1");
    private final BrennonUserLogin login = BrennonUserLogin.of("john@mail.com");

    @Test
    @DisplayName("Has no profile image by default")
    public void givenNewProfile_whenProfileImageQueried_thenEmptyResultReturned() {
        var profile = new BrennonUserProfile(userId, 0, login, "John", "Smith", null);

        Assertions.assertThat(profile.getProfileImageFileId()).isEmpty();
    }

    @Test
    @DisplayName("Updates profile image file reference")
    public void givenProfile_whenProfileImageUpdated_thenNewFileReferenceIsStored() {
        var fileId = FileId.of("file-1");
        var profile = new BrennonUserProfile(userId, 0, login, "John", "Smith", null);

        profile.updateProfileImage(fileId);

        Assertions.assertThat(profile.getProfileImageFileId()).contains(fileId);
    }

    @Test
    @DisplayName("Rejects missing profile image file reference")
    public void givenProfile_whenNullProfileImageUpdated_thenIllegalArgumentReported() {
        var profile = new BrennonUserProfile(userId, 0, login, "John", "Smith", null);

        Assertions.assertThatThrownBy(() -> profile.updateProfileImage(null))
            .isInstanceOf(IllegalArgumentException.class);
    }
}
