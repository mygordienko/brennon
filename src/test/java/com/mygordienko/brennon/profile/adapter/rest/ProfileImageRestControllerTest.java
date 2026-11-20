package com.mygordienko.brennon.profile.adapter.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.util.unit.DataSize;

import com.mygordienko.brennon.core.common.FileMeta;
import com.mygordienko.brennon.core.common.ResourceBundle;
import com.mygordienko.brennon.core.common.adapter.FileContentValidator;
import com.mygordienko.brennon.profile.internal.ProfileImageService;

public class ProfileImageRestControllerTest {

    private final ProfileImageService profileImageService = mock(ProfileImageService.class);

    private ProfileImageRestController controller;

    @BeforeEach
    public void setUp() {
        controller = new ProfileImageRestController(
                profileImageService,
                mock(FileContentValidator.class),
                DataSize.ofMegabytes(5),
                DataSize.ofMegabytes(5));
    }

    @Test
    @DisplayName("Profile image is privately cached by the browser")
    public void givenProfileImage_whenImageRequested_thenResponseIsPrivatelyCacheable() {
        when(profileImageService.getCurrentUserProfileImage())
                .thenReturn(Optional.of(new ResourceBundle(
                        new ByteArrayResource("image".getBytes()), new FileMeta("image.png"))));

        var response = controller.getImage();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getHeaders().getCacheControl())
                .contains("private")
                .contains("max-age=600")
                .doesNotContain("no-store");
        // NOTE: image is per user
        assertThat(response.getHeaders().getVary()).contains(HttpHeaders.COOKIE);
    }

    @Test
    @DisplayName("Absent profile image must not be cached")
    public void givenNoProfileImage_whenImageRequested_thenResponseIsNotCacheable() {
        when(profileImageService.getCurrentUserProfileImage()).thenReturn(Optional.empty());

        var response = controller.getImage();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getHeaders().getCacheControl()).isEqualTo("no-store");
    }
}
