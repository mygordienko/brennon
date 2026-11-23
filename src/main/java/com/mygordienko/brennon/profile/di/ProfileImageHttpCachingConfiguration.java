package com.mygordienko.brennon.profile.di;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;
import org.springframework.security.web.util.matcher.OrRequestMatcher;

/**
 * Profile images are private, but they do not change often, so browsers are allowed to cache them
 * for a short period of time (see `ProfileImageRestController`).
 *
 * NOTE: Spring Security forbids HTTP caching for all endpoints by default and applies its default
 * headers to every security filter chain. Hence, relaxing caching for a single endpoint requires a
 * dedicated security filter chain that matches this endpoint only; the actual `Cache-Control`
 * header is set on successful responses by the controller.
 */
@Configuration
public class ProfileImageHttpCachingConfiguration {

    static final String PROFILE_IMAGES_ENDPOINT = "/api/v1/profile/images";

    @Bean
    // NOTE: must be evaluated before the default security filter chain
    @Order(1)
    public SecurityFilterChain profileImageSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .securityMatcher(new OrRequestMatcher(
                        PathPatternRequestMatcher.pathPattern(HttpMethod.GET, PROFILE_IMAGES_ENDPOINT + "/me"),
                        PathPatternRequestMatcher.pathPattern(HttpMethod.GET, PROFILE_IMAGES_ENDPOINT + "/{profileId}")))
                .authorizeHttpRequests(authorize -> authorize
                        .anyRequest().authenticated()
                )
                .headers(headers -> headers
                        .cacheControl(HeadersConfigurer.CacheControlConfig::disable)
                );

        return http.build();
    }
}
