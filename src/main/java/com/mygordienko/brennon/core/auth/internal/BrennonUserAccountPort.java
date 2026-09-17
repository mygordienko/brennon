package com.mygordienko.brennon.core.auth.internal;

import java.util.Optional;

import com.mygordienko.brennon.core.auth.BrennonUserAccount;
import com.mygordienko.brennon.core.auth.BrennonUserLogin;

/**
 * Abstraction to get username of the currently authenticated user (to avoid calling Spring's context directly in higher-level classes).
 */
public interface BrennonUserAccountPort {
    public Optional<BrennonUserLogin> getCurrentUsername();
    public Optional<BrennonUserAccount> getUserAccount(BrennonUserLogin login);
}
