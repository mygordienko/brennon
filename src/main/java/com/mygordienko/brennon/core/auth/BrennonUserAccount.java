package com.mygordienko.brennon.core.auth;

import java.util.Set;

/**
 * Represents user's authorization data (roles, permissions)
 * This class does not contain any personal or business-related data
 */
public class BrennonUserAccount {
    public BrennonUserAccount(BrennonUserLogin login, Set<String> authorities) {
        this.login = login;
        this.authorities = Set.copyOf(authorities);
    }

    private BrennonUserLogin login;
    public BrennonUserLogin getLogin() {
        return login;
    }

    private Set<String> authorities;

    public boolean isSupport() {
        return authorities.contains("support:read") && authorities.contains("support:write");
    }
}
