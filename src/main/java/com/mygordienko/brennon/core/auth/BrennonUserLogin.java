package com.mygordienko.brennon.core.auth;

/**
 * Represents a user login in Brennon app.
 * @param value String representation.
 */
public record BrennonUserLogin(String value) {
    @Override
    public String toString() {
        return value;
    } 
}
