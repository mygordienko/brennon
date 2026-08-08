package com.mygordienko.brennon.core.auth;

import java.util.Optional;

import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Abstraction to get username of the currently authenticated user (to avoid calling Spring's context directly in higher-level classes).
 */
public interface CurrentUserPort {
    default public Optional<BrennonUserLogin> getCurrentUsername() {
        return Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication())
            .filter(auth -> auth.isAuthenticated())
            .map(auth -> auth.getName())
            .map(username -> new BrennonUserLogin(username));
    }
}
