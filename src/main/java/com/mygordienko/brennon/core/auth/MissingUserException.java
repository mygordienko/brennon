package com.mygordienko.brennon.core.auth;

public class MissingUserException extends RuntimeException {
    public MissingUserException(String message) {
        super(message);
    }

    private static final String MESSAGE_TEMPLATE_ID = "Missing user with id: %s";

    public static MissingUserException forId(BrennonUserId userId) {
        return new MissingUserException(String.format(MESSAGE_TEMPLATE_ID, userId));
    }

    private static final String MESSAGE_LOGIN_TEMPALTE = "Missing user with specified login %s";

    public static MissingUserException forLogin(BrennonUserLogin login) {
        return new MissingUserException(String.format(MESSAGE_LOGIN_TEMPALTE, login));
    }
}
