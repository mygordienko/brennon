package com.mygordienko.brennon.profile.internal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.util.Optional;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.core.io.ByteArrayResource;

import com.mygordienko.brennon.core.auth.BrennonUserAccountApi;
import com.mygordienko.brennon.core.auth.BrennonUserId;
import com.mygordienko.brennon.core.auth.BrennonUserLogin;
import com.mygordienko.brennon.core.common.FileId;
import com.mygordienko.brennon.core.common.FileMeta;
import com.mygordienko.brennon.core.common.ResourceBundle;
import com.mygordienko.brennon.profile.BrennonUserProfile;
import com.mygordienko.brennon.profile.ProfileConcurrentModificationException;

public class ProfileImageServiceTest {

    private final ProfileImageStoragePort profileImageStoragePort = mock(ProfileImageStoragePort.class);
    private final BrennonUserProfilePort userProfilePort = mock(BrennonUserProfilePort.class);
    private final BrennonUserAccountApi userAccountApi = mock(BrennonUserAccountApi.class);

    private final BrennonUserId userId = BrennonUserId.of("user-1");
    private final BrennonUserLogin login = BrennonUserLogin.of("john@mail.com");

    private ProfileImageService profileImageService;

    @BeforeEach
    public void setUp() {
        var currentUser = new BrennonUserProfile(userId, 0, login, "John", "Smith", null);

        when(userAccountApi.getCurrentUsername()).thenReturn(Optional.of(login));
        when(userProfilePort.getByLogin(login)).thenReturn(Optional.of(currentUser));
        when(userProfilePort.getById(userId)).thenReturn(Optional.of(currentUser));
        when(userProfilePort.save(any(BrennonUserProfile.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

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
    @DisplayName("Updates profile image through the aggregate and saves it")
    public void givenMatchingVersion_whenProfileImageSaved_thenMovesFileAndUpdatesAggregate() {
        var fileId = FileId.of("file-1");

        profileImageService.saveProfileImage(fileId, 0);

        verify(profileImageStoragePort).moveToFinal(userId, fileId);

        var profileCaptor = ArgumentCaptor.forClass(BrennonUserProfile.class);
        verify(userProfilePort).save(profileCaptor.capture());

        Assertions.assertThat(profileCaptor.getValue().getProfileImageFileId()).contains(fileId);
    }

    @Test
    @DisplayName("Rejects stale version when saving profile image")
    public void givenStaleVersion_whenProfileImageSaved_thenConcurrentModificationReported() {
        var fileId = FileId.of("file-1");

        Assertions.assertThatThrownBy(() -> profileImageService.saveProfileImage(fileId, 10))
            .isInstanceOf(ProfileConcurrentModificationException.class);

        verify(profileImageStoragePort, never()).moveToFinal(any(), any());
        verify(userProfilePort, never()).save(any());
    }

    @Test
    @DisplayName("Checks and loads current user's profile image")
    public void givenCurrentUser_whenProfileImageQueried_thenDelegatesToStorageWithCurrentUserImageFile() {
        var fileId = FileId.of("file-1");
        var resourceBundle = new ResourceBundle(
            new ByteArrayResource("image".getBytes()), new FileMeta("image.png"));

        var currentUser = new BrennonUserProfile(userId, 0, login, "John", "Smith", fileId);
        when(userProfilePort.getByLogin(login)).thenReturn(Optional.of(currentUser));
        when(profileImageStoragePort.getImageResource(userId, fileId)).thenReturn(Optional.of(resourceBundle));

        Assertions.assertThat(profileImageService.currentUserHasProfileImage()).isTrue();
        Assertions.assertThat(profileImageService.getCurrentUserProfileImage()).contains(resourceBundle);
    }

    @Test
    @DisplayName("Reports no profile image when the aggregate has no image file")
    public void givenCurrentUserWithoutImage_whenProfileImageQueried_thenNoImageReported() {
        Assertions.assertThat(profileImageService.currentUserHasProfileImage()).isFalse();
        Assertions.assertThat(profileImageService.getCurrentUserProfileImage()).isEmpty();
    }

    @Test
    @DisplayName("Loads profile image of an arbitrary user")
    public void givenProfileId_whenProfileImageQueried_thenDelegatesToStorageWithProvidedUserAndImageFile() {
        var targetUserId = BrennonUserId.of("user-2");
        var fileId = FileId.of("file-2");
        var resourceBundle = new ResourceBundle(
            new ByteArrayResource("image".getBytes()), new FileMeta("image.png"));

        when(userProfilePort.getById(targetUserId)).thenReturn(Optional.of(
            new BrennonUserProfile(targetUserId, 0, login, "John", "Smith", fileId)));
        when(profileImageStoragePort.getImageResource(targetUserId, fileId)).thenReturn(Optional.of(resourceBundle));

        Assertions.assertThat(profileImageService.getProfileImage(targetUserId)).contains(resourceBundle);
    }

    @Test
    @DisplayName("Returns no image for a missing user")
    public void givenMissingProfile_whenProfileImageQueried_thenEmptyResultReturned() {
        var targetUserId = BrennonUserId.of("user-2");

        when(userProfilePort.getById(targetUserId)).thenReturn(Optional.empty());

        Assertions.assertThat(profileImageService.getProfileImage(targetUserId)).isEmpty();
    }
}
