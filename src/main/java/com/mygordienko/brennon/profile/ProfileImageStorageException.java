package com.mygordienko.brennon.profile;

public class ProfileImageStorageException extends RuntimeException {
    public ProfileImageStorageException(String message) {
        super(message);
    }

    public ProfileImageStorageException(String message, Throwable cause) {
        super(message, cause);
    }

    public ProfileImageStorageException(Throwable cause) {
        super(cause);
    }
}
