package com.mygordienko.brennon.profile.internal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
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
import com.mygordienko.brennon.core.common.ClockManager;
import com.mygordienko.brennon.core.common.FileId;
import com.mygordienko.brennon.core.common.FileMeta;
import com.mygordienko.brennon.core.common.ResourceBundle;
import com.mygordienko.brennon.profile.BrennonUserProfile;
import com.mygordienko.brennon.profile.ProfileConcurrentModificationException;
import com.mygordienko.brennon.profile.ProfileImageStorageException;
import com.mygordienko.brennon.profile.ProfileImageUpdatedEvent;

public class ProfileImageServiceTest {

    private final ProfileImageStoragePort profileImageStoragePort = mock(ProfileImageStoragePort.class);
    private final BrennonUserProfilePort userProfilePort = mock(BrennonUserProfilePort.class);
    private final BrennonUserAccountApi userAccountApi = mock(BrennonUserAccountApi.class);
    private final ClockManager clockManager = mock(ClockManager.class);

    private final BrennonUserId userId = BrennonUserId.of("user-1");
    private final BrennonUserLogin login = BrennonUserLogin.of("john@mail.com");
    private final Instant now = Instant.parse("2026-08-30T13:35:00Z");

    private ProfileImageService profileImageService;

    @BeforeEach
    public void setUp() {
        var created = OffsetDateTime.parse("2026-08-30T15:30:00+02:00");
        var currentUser = new BrennonUserProfile(userId, 0, login, "John", "Smith", null, created, created);

        when(clockManager.now()).thenReturn(now);
        when(userAccountApi.getCurrentUsername()).thenReturn(Optional.of(login));
        when(userProfilePort.getByLogin(login)).thenReturn(Optional.of(currentUser));
        when(userProfilePort.getById(userId)).thenReturn(Optional.of(currentUser));
        when(userProfilePort.save(any(BrennonUserProfile.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        var userProfileService = new BrennonUserProfileService(userProfilePort, userAccountApi);
        profileImageService = new ProfileImageService(profileImageStoragePort, userProfileService, clockManager);
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
        Assertions.assertThat(profileCaptor.getValue().getUpdated())
            .isEqualTo(OffsetDateTime.ofInstant(now, ZoneId.systemDefault()));
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
    @DisplayName("Evicts previous profile image after update")
    public void givenPreviousImage_whenProfileImageUpdatedEventProcessed_thenPreviousFileDeleted() {
        var previousFileId = FileId.of("file-1");
        var newFileId = FileId.of("file-2");

        profileImageService.processProfileImageUpdated(
            new ProfileImageUpdatedEvent(userId, previousFileId, newFileId));

        verify(profileImageStoragePort).delete(userId, previousFileId);
    }

    @Test
    @DisplayName("Does not evict an image when there was no previous image")
    public void givenNoPreviousImage_whenProfileImageUpdatedEventProcessed_thenNothingDeleted() {
        profileImageService.processProfileImageUpdated(
            new ProfileImageUpdatedEvent(userId, null, FileId.of("file-1")));

        verify(profileImageStoragePort, never()).delete(any(), any());
    }

    @Test
    @DisplayName("Ignores storage errors during best-effort image eviction")
    public void givenStorageFailure_whenProfileImageUpdatedEventProcessed_thenNoExceptionPropagated() {
        var previousFileId = FileId.of("file-1");
        var newFileId = FileId.of("file-2");

        doThrow(new ProfileImageStorageException("storage unavailable"))
            .when(profileImageStoragePort).delete(userId, previousFileId);

        Assertions.assertThatCode(() -> profileImageService.processProfileImageUpdated(
                new ProfileImageUpdatedEvent(userId, previousFileId, newFileId)))
            .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Loads current profile image when requested file is the current one")
    public void givenCurrentImageFile_whenProfileImageQueried_thenDelegatesToStorage() {
        var targetUserId = BrennonUserId.of("user-2");
        var fileId = FileId.of("file-2");
        var created = OffsetDateTime.parse("2026-08-30T15:30:00+02:00");
        var resourceBundle = new ResourceBundle(
            new ByteArrayResource("image".getBytes()), new FileMeta("image.png"));

        when(userProfilePort.getById(targetUserId)).thenReturn(Optional.of(
            new BrennonUserProfile(targetUserId, 0, login, "John", "Smith", fileId, created, created)));
        when(profileImageStoragePort.getImageResource(targetUserId, fileId)).thenReturn(Optional.of(resourceBundle));

        Assertions.assertThat(profileImageService.getProfileImage(targetUserId, fileId)).contains(resourceBundle);
    }

    @Test
    @DisplayName("Rejects request for a profile image that is not current")
    public void givenOutdatedImageFile_whenProfileImageQueried_thenIllegalArgumentReported() {
        var targetUserId = BrennonUserId.of("user-2");
        var currentFileId = FileId.of("file-2");
        var requestedFileId = FileId.of("file-1");
        var created = OffsetDateTime.parse("2026-08-30T15:30:00+02:00");

        when(userProfilePort.getById(targetUserId)).thenReturn(Optional.of(
            new BrennonUserProfile(targetUserId, 0, login, "John", "Smith", currentFileId, created, created)));

        Assertions.assertThatThrownBy(() -> profileImageService.getProfileImage(targetUserId, requestedFileId))
            .isInstanceOf(IllegalArgumentException.class);

        verify(profileImageStoragePort, never()).getImageResource(any(), any());
    }

    @Test
    @DisplayName("Returns no image when profile has no image set")
    public void givenProfileWithoutImage_whenProfileImageQueried_thenEmptyResultReturned() {
        Assertions.assertThat(profileImageService.getProfileImage(userId, FileId.of("file-1"))).isEmpty();

        verify(profileImageStoragePort, never()).getImageResource(any(), any());
    }

    @Test
    @DisplayName("Returns no image for a missing user")
    public void givenMissingProfile_whenProfileImageQueried_thenEmptyResultReturned() {
        var targetUserId = BrennonUserId.of("user-2");

        when(userProfilePort.getById(targetUserId)).thenReturn(Optional.empty());

        Assertions.assertThat(profileImageService.getProfileImage(targetUserId, FileId.of("file-1"))).isEmpty();
    }
}
