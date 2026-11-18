package com.mygordienko.brennon.core.auth;

import java.util.Optional;

public interface BrennonUserAccountApi {
    public Optional<BrennonUserLogin> getCurrentUsername();
    public Optional<BrennonUserAccount> getUserAccount(BrennonUserLogin login);
}
