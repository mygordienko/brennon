package com.mygordienko.brennon.core.auth;

import lombok.EqualsAndHashCode;

/**
 * Represents a user login in Brennon app.
 */
@EqualsAndHashCode(of = {"value"})
public final class BrennonUserLogin {
    public BrennonUserLogin(String value) {
        this.value = value;
    }

    public static BrennonUserLogin of(String value) {
        return new BrennonUserLogin(value);
    }

    private final String value;

    @Override
    public String toString() {
        return value;
    } 
}
