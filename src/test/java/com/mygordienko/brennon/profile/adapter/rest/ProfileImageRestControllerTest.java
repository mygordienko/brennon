package com.mygordienko.brennon.profile.adapter.rest;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.mygordienko.brennon.core.auth.BrennonUserId;
import com.mygordienko.brennon.core.auth.di.CoreSecurityConfiguration;
import com.mygordienko.brennon.core.common.FileId;
import com.mygordienko.brennon.core.common.FileMeta;
import com.mygordienko.brennon.core.common.ResourceBundle;
import com.mygordienko.brennon.core.common.adapter.FileContentValidator;
import com.mygordienko.brennon.profile.di.ProfileImageHttpCachingConfiguration;
import com.mygordienko.brennon.profile.internal.ProfileImageService;

/**
 * NOTE: feature-specific security configuration is imported to verify that it does not clash with
 * the caching headers produced by the controller.
 */
@WebMvcTest(ProfileImageRestController.class)
@Import({CoreSecurityConfiguration.class, ProfileImageHttpCachingConfiguration.class})
public class ProfileImageRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProfileImageService profileImageService;

    @MockitoBean
    private FileContentValidator fileContentValidator;

    /**
     * NOTE: replace the origin UserDetailsService bean from CoreSecurityConfig
     */
    @MockitoBean
    private UserDetailsService userDetailsService;

    @Test
    @DisplayName("Profile image is privately cached by the browser for one month")
    public void givenProfileImage_whenImageRequested_thenResponseIsPrivatelyCacheable() throws Exception {
        when(profileImageService.getProfileImage(BrennonUserId.of("2"), FileId.of("file-1")))
                .thenReturn(Optional.of(new ResourceBundle(
                        new ByteArrayResource("image".getBytes()), new FileMeta("image.png"))));

        mockMvc.perform(get("/api/v1/profile/2/images/file-1").with(user("john")
                        .authorities(new SimpleGrantedAuthority("user:read"))))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CACHE_CONTROL,
                        allOf(containsString("private"), containsString("max-age=2592000"))))
                // NOTE: image is per user
                .andExpect(header().string(HttpHeaders.VARY, containsString(HttpHeaders.COOKIE)))
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, "image/png"));
    }

    @Test
    @DisplayName("Absent profile image must not be cached")
    public void givenNoProfileImage_whenImageRequested_thenResponseIsNotCacheable() throws Exception {
        when(profileImageService.getProfileImage(BrennonUserId.of("2"), FileId.of("file-1")))
                .thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/profile/2/images/file-1").with(user("john")
                        .authorities(new SimpleGrantedAuthority("user:read"))))
                .andExpect(status().isNotFound())
                .andExpect(header().string(HttpHeaders.CACHE_CONTROL, "no-store"));
    }

    @Test
    @DisplayName("Outdated profile image must not be served or cached")
    public void givenOutdatedImageFile_whenImageRequested_thenNotFoundReturned() throws Exception {
        when(profileImageService.getProfileImage(BrennonUserId.of("2"), FileId.of("file-1")))
                .thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/profile/2/images/file-1").with(user("john")
                        .authorities(new SimpleGrantedAuthority("user:read"))))
                .andExpect(status().isNotFound())
                .andExpect(header().string(HttpHeaders.CACHE_CONTROL, "no-store"));
    }

    @Test
    @DisplayName("Profile image is not available without authentication")
    public void givenNoAuthentication_whenImageRequested_thenAccessIsDenied() throws Exception {
        mockMvc.perform(get("/api/v1/profile/2/images/file-1"))
                .andExpect(status().isForbidden());
    }
}
