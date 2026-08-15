package com.mygordienko.brennon.core.auth;

/**
 * Represents a user ID in Brennon app.
 */
public final class BrennonUserId {
    private BrennonUserId(String value) {
        this.value = value;
    }

    public static BrennonUserId of(String val) {
        return new BrennonUserId(val);
    }

    private final String value;

    @Override
    public String toString() {
        return value;
    }
}
