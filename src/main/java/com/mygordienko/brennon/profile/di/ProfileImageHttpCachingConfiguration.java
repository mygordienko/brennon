package com.mygordienko.brennon.profile.di;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Profile image of the current user is private, but it does not change often, so browsers are
 * allowed to cache it for a short period of time (see `ProfileImageRestController`).
 *
 * NOTE: Spring Security forbids HTTP caching for all endpoints by default and applies its default
 * headers to every security filter chain. Hence, relaxing caching for a single endpoint requires a
 * dedicated security filter chain that matches this endpoint only; the actual `Cache-Control`
 * header is set on successful responses by the controller.
 */
@Configuration
public class ProfileImageHttpCachingConfiguration {

    static final String PROFILE_IMAGE_ENDPOINT = "/api/v1/profile/image";

    @Bean
    // NOTE: must be evaluated before the default security filter chain
    @Order(1)
    public SecurityFilterChain profileImageSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .securityMatcher(PROFILE_IMAGE_ENDPOINT)
                .authorizeHttpRequests(authorize -> authorize
                        .anyRequest().authenticated()
                )
                .headers(headers -> headers
                        .cacheControl(HeadersConfigurer.CacheControlConfig::disable)
                );

        return http.build();
    }
}
