package com.mygordienko.brennon.profile.adapter.web;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.OffsetDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.mygordienko.brennon.core.auth.BrennonUserId;
import com.mygordienko.brennon.core.auth.BrennonUserLogin;
import com.mygordienko.brennon.core.auth.di.CoreSecurityConfiguration;
import com.mygordienko.brennon.core.common.di.ThymeleafCustomConfiguration;
import com.mygordienko.brennon.profile.BrennonUserProfile;
import com.mygordienko.brennon.profile.ProfileConcurrentModificationException;
import com.mygordienko.brennon.profile.internal.BrennonUserProfileService;
import com.mygordienko.brennon.profile.internal.ProfileImageService;

@WebMvcTest(ProfileImagePageController.class)
@Import({CoreSecurityConfiguration.class, ThymeleafCustomConfiguration.class})
public class ProfileImagePageControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProfileImageService profileImageService;

    @MockitoBean
    private BrennonUserProfileService userProfileService;

    /**
     * NOTE: replace the origin UserDetailsService bean from CoreSecurityConfig
     */
    @MockitoBean
    private UserDetailsService userDetailsService;

    private final BrennonUserProfile profile = new BrennonUserProfile(
        BrennonUserId.of("1"), 3, BrennonUserLogin.of("john@mail.com"), "John", "Smith", null,
        OffsetDateTime.parse("2026-08-30T15:30:00+02:00"),
        OffsetDateTime.parse("2026-08-30T15:30:00+02:00"));

    @Test
    @DisplayName("Renders aggregate version in the profile image form")
    public void givenProfile_whenProfilePageRequested_thenFormContainsVersion() throws Exception {
        when(userProfileService.getCurrentUser()).thenReturn(profile);

        mockMvc.perform(get("/profile").with(user("john@mail.com").authorities(
                new SimpleGrantedAuthority("user:read"),
                new SimpleGrantedAuthority("user:write"))))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("name=\"version\"")))
            .andExpect(content().string(containsString("value=\"3\"")));
    }

    @Test
    @DisplayName("Rejects stale profile version on image update")
    public void givenStaleVersion_whenProfileImageUpdated_thenConflictReported() throws Exception {
        doThrow(ProfileConcurrentModificationException.forProfile(profile.getId()))
            .when(profileImageService).saveProfileImage(any(), eq(2));

        mockMvc.perform(post("/profile/image").with(user("john@mail.com").authorities(
                new SimpleGrantedAuthority("user:read"),
                new SimpleGrantedAuthority("user:write")))
                .with(csrf())
                .param("version", "2")
                .param("fileId", "file-1"))
            .andExpect(status().isConflict());
    }
}
