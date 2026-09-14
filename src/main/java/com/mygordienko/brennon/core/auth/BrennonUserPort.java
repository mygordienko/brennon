package com.mygordienko.brennon.core.auth;

import java.util.Optional;

/**
 * Abstraction to get username of the currently authenticated user (to avoid calling Spring's context directly in higher-level classes).
 */
public interface BrennonUserPort {
    public Optional<BrennonUserLogin> getCurrentUsername();
    public Optional<BrennonUserAccount> getUserAccount(BrennonUserLogin login);
}
