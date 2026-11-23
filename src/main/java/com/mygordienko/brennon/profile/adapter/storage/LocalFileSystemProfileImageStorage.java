package com.mygordienko.brennon.profile.adapter.storage;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Optional;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;

import com.mygordienko.brennon.core.auth.BrennonUserId;
import com.mygordienko.brennon.core.common.FileId;
import com.mygordienko.brennon.core.common.FileMeta;
import com.mygordienko.brennon.core.common.ResourceBundle;
import com.mygordienko.brennon.profile.ProfileImageStorageException;
import com.mygordienko.brennon.profile.internal.ProfileImageStoragePort;

public class LocalFileSystemProfileImageStorage implements ProfileImageStoragePort {

    public LocalFileSystemProfileImageStorage(Path baseFolder) {
        this.baseFolder = baseFolder;
    }

    private static final String ORIG_FILE_NAME_CUSTOM_ATTRIBUTE = "user:orig.filename";

    private final Path baseFolder;
    private final Path tmpBaseFolder = Paths.get("tmp");
    private final Path profilesBaseFolder = Paths.get("profiles");

    @Override
    public void saveTemp(BrennonUserId user, FileId fileId, InputStream is, String fileName) throws ProfileImageStorageException {
        var target = getTempPathForUser(user, fileId);

        try {
            Files.createDirectories(target.getParent());
            Files.copy(is, target, StandardCopyOption.REPLACE_EXISTING);
            writeCustomAttribute(target, ORIG_FILE_NAME_CUSTOM_ATTRIBUTE, fileName);
        } catch (IOException e) {
            throw new ProfileImageStorageException(e);
        }
    }

    @Override
    public void moveToFinal(BrennonUserId user, FileId fileId) throws ProfileImageStorageException {
        var source = getTempPathForUser(user, fileId);
        var target = getFinalPathForUser(user);

        try (var is = Files.newInputStream(source)) {
            Files.createDirectories(target.getParent());
            Files.copy(is, target, StandardCopyOption.REPLACE_EXISTING);
            // NOTE: StandardCopyOption.COPY_ATTRIBUTES does not guarantee copying of custom user-defined attributes
            copyCustomAttributes(source, target);
        } catch (IOException e) {
            throw new ProfileImageStorageException(e);
        }
    }

    @Override
    public boolean exists(BrennonUserId user) throws ProfileImageStorageException {
        return Files.exists(getFinalPathForUser(user));
    }

    @Override
    public Optional<ResourceBundle> getImageResource(BrennonUserId user) throws ProfileImageStorageException {
        var target = getFinalPathForUser(user);

        if (!Files.exists(target)) {
            return Optional.empty();
        }

        try {
            Resource resource = new UrlResource(target.toUri());

            if (!resource.isReadable()) {
                throw new ProfileImageStorageException("Profile image not readable for user: %s".formatted(user));
            }

            var originalFilename = readCustomAttribute(target, ORIG_FILE_NAME_CUSTOM_ATTRIBUTE);

            return Optional.of(new ResourceBundle(resource, new FileMeta(originalFilename)));
        } catch (IOException e) {
            throw new ProfileImageStorageException("Could not read profile image for user: %s".formatted(user), e);
        }
    }

    private Path getTempPathForUser(BrennonUserId userId, FileId fileId) {
        return baseFolder.resolve(tmpBaseFolder).resolve(userId.toString()).resolve(fileId.toString());
    }

    private Path getFinalPathForUser(BrennonUserId userId) {
        var profilesFolder = baseFolder.resolve(profilesBaseFolder).normalize();
        var target = profilesFolder.resolve(userId.toString()).normalize();

        if (!target.startsWith(profilesFolder) || target.equals(profilesFolder)) {
            throw new IllegalArgumentException("Invalid profile image owner: %s".formatted(userId));
        }

        return target;
    }

    private void copyCustomAttributes(Path source, Path target) throws IOException {
        var fileName = readCustomAttribute(source, ORIG_FILE_NAME_CUSTOM_ATTRIBUTE);
        writeCustomAttribute(target, ORIG_FILE_NAME_CUSTOM_ATTRIBUTE, fileName);
    }

    private void writeCustomAttribute(Path target, String attribute, String value) throws IOException {
        Files.setAttribute(target, attribute, StandardCharsets.UTF_8.encode(value));
    }

    private String readCustomAttribute(Path target, String attribute) throws IOException {
        byte[] bytes = (byte[]) Files.getAttribute(target, attribute);
        return StandardCharsets.UTF_8.decode(ByteBuffer.wrap(bytes)).toString();
    }
}
