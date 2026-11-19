package com.mygordienko.brennon.profile.internal;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.util.Optional;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ByteArrayResource;

import com.mygordienko.brennon.core.auth.BrennonUserAccountApi;
import com.mygordienko.brennon.core.auth.BrennonUserId;
import com.mygordienko.brennon.core.auth.BrennonUserLogin;
import com.mygordienko.brennon.core.common.FileId;
import com.mygordienko.brennon.core.common.FileMeta;
import com.mygordienko.brennon.core.common.ResourceBundle;
import com.mygordienko.brennon.profile.BrennonUserProfile;

public class ProfileImageServiceTest {

    private final ProfileImageStoragePort profileImageStoragePort = mock(ProfileImageStoragePort.class);
    private final BrennonUserProfilePort userProfilePort = mock(BrennonUserProfilePort.class);
    private final BrennonUserAccountApi userAccountApi = mock(BrennonUserAccountApi.class);

    private final BrennonUserId userId = BrennonUserId.of("user-1");
    private final BrennonUserLogin login = BrennonUserLogin.of("john@mail.com");

    private ProfileImageService profileImageService;

    @BeforeEach
    public void setUp() {
        when(userAccountApi.getCurrentUsername()).thenReturn(Optional.of(login));
        when(userProfilePort.getByLogin(login)).thenReturn(Optional.of(
            new BrennonUserProfile(userId, login, "John", "Smith")));

        var userProfileService = new BrennonUserProfileService(userProfilePort, userAccountApi);
        profileImageService = new ProfileImageService(profileImageStoragePort, userProfileService);
    }

    @Test
    @DisplayName("Saves temp image for the current user")
    public void givenCurrentUser_whenTempImageSaved_thenDelegatesToStorageWithCurrentUserId() {
        var fileId = FileId.of("file-1");
        var is = new ByteArrayInputStream("image".getBytes());

        profileImageService.saveTemp(fileId, is, "image.png");

        verify(profileImageStoragePort).saveTemp(userId, fileId, is, "image.png");
    }

    @Test
    @DisplayName("Moves uploaded image to final storage for the current user")
    public void givenCurrentUser_whenProfileImageSaved_thenMovesTempImageToFinalStorage() {
        var fileId = FileId.of("file-1");

        profileImageService.saveProfileImage(fileId);

        verify(profileImageStoragePort).moveToFinal(userId, fileId);
    }

    @Test
    @DisplayName("Checks and loads current user's profile image")
    public void givenCurrentUser_whenProfileImageQueried_thenDelegatesToStorageWithCurrentUserId() {
        var resourceBundle = new ResourceBundle(
            new ByteArrayResource("image".getBytes()), new FileMeta("image.png"));

        when(profileImageStoragePort.exists(userId)).thenReturn(true);
        when(profileImageStoragePort.getImageResource(userId)).thenReturn(Optional.of(resourceBundle));

        Assertions.assertThat(profileImageService.currentUserHasProfileImage()).isTrue();
        Assertions.assertThat(profileImageService.getCurrentUserProfileImage()).contains(resourceBundle);
    }
}
