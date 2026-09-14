package com.mygordienko.brennon.core.auth;

import java.util.Set;

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
