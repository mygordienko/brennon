package com.mygordienko.brennon.profile.internal;

import java.io.InputStream;
import java.util.Optional;

import com.mygordienko.brennon.core.auth.BrennonUserId;
import com.mygordienko.brennon.core.common.FileId;
import com.mygordienko.brennon.core.common.ResourceBundle;
import com.mygordienko.brennon.profile.ProfileImageStorageException;

public interface ProfileImageStoragePort {
    void saveTemp(BrennonUserId user, FileId fileId, InputStream is, String fileName) throws ProfileImageStorageException;
    void moveToFinal(BrennonUserId user, FileId fileId) throws ProfileImageStorageException;
    void delete(BrennonUserId user, FileId fileId) throws ProfileImageStorageException;
    Optional<ResourceBundle> getImageResource(BrennonUserId user, FileId fileId) throws ProfileImageStorageException;
}
