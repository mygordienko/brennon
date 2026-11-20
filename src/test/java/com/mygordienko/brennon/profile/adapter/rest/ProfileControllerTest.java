package com.mygordienko.brennon.profile.adapter.rest;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.mygordienko.brennon.core.auth.di.CoreSecurityConfiguration;
import com.mygordienko.brennon.core.common.adapter.FileContentValidator;
import com.mygordienko.brennon.profile.di.ProfileImageHttpCachingConfiguration;
import com.mygordienko.brennon.profile.internal.ProfileImageService;

/**
 * These tests are related to REST profile image endpoints because we disable cache for them
 * and need to make sure other endpoints still have it.
 */
@WebMvcTest(ProfileImageRestController.class)
@Import({CoreSecurityConfiguration.class, ProfileImageHttpCachingConfiguration.class})
public class ProfileControllerTest {
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
    public void otherEndpointsAreStillNotCacheable() throws Exception {
      mockMvc.perform(get("/profile").with(user("john").authorities(
          new SimpleGrantedAuthority("user:read"),
          new SimpleGrantedAuthority("user:write"))))
        .andExpect(header().string(HttpHeaders.CACHE_CONTROL, containsString("no-store")))
        .andExpect(header().string(HttpHeaders.CACHE_CONTROL, not(containsString("private"))));
    }
}
