package com.mygordienko.brennon.core.auth;

/**
 * Represents a user login in Brennon app.
 */
public final class BrennonUserLogin {
    public BrennonUserLogin(String value) {
        this.value = value;
    }

    public static BrennonUserLogin from(String value) {
        return new BrennonUserLogin(value);
    }

    private final String value;

    @Override
    public String toString() {
        return value;
    } 
}
