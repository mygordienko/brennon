package com.mygordienko.brennon.core.auth;

import lombok.EqualsAndHashCode;

/**
 * Represents a user ID in Brennon app.
 */
@EqualsAndHashCode(of = {"value"})
public final class BrennonUserId implements Comparable<BrennonUserId> {
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

    @Override
    public int compareTo(BrennonUserId o) {
        return this.value.compareTo(o.value);
    }

}
