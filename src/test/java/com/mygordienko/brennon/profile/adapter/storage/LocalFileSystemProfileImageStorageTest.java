package com.mygordienko.brennon.profile.adapter.storage;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.mygordienko.brennon.core.auth.BrennonUserId;
import com.mygordienko.brennon.core.common.FileId;

public class LocalFileSystemProfileImageStorageTest {

    @TempDir
    java.nio.file.Path baseFolder;

    @Test
    @DisplayName("Stores temp image and moves it to final storage")
    public void givenUploadedImage_whenMovedToFinal_thenImageIsAvailable() throws Exception {
        var storage = new LocalFileSystemProfileImageStorage(baseFolder);
        var userId = BrennonUserId.of("user-1");
        var fileId = FileId.of("file-1");
        var content = "profile-image-content".getBytes(StandardCharsets.UTF_8);

        storage.saveTemp(userId, fileId, new ByteArrayInputStream(content), "profile.png");

        Assertions.assertThat(storage.getImageResource(userId, fileId)).isEmpty();

        storage.moveToFinal(userId, fileId);

        var resourceBundle = storage.getImageResource(userId, fileId).orElseThrow();

        Assertions.assertThat(resourceBundle.meta().fileName()).isEqualTo("profile.png");
        try (var is = resourceBundle.resource().getInputStream()) {
            Assertions.assertThat(is.readAllBytes()).isEqualTo(content);
        }
    }

    @Test
    @DisplayName("Keeps previously stored image when a new one is moved to final storage")
    public void givenExistingImage_whenNewImageMovedToFinal_thenBothImagesAreAvailable() throws Exception {
        var storage = new LocalFileSystemProfileImageStorage(baseFolder);
        var userId = BrennonUserId.of("user-1");
        var oldFileId = FileId.of("file-1");
        var newFileId = FileId.of("file-2");
        var oldContent = "old-image".getBytes(StandardCharsets.UTF_8);
        var newContent = "new-image".getBytes(StandardCharsets.UTF_8);

        storage.saveTemp(userId, oldFileId, new ByteArrayInputStream(oldContent), "old.png");
        storage.moveToFinal(userId, oldFileId);

        storage.saveTemp(userId, newFileId, new ByteArrayInputStream(newContent), "new.jpg");
        storage.moveToFinal(userId, newFileId);

        var oldResourceBundle = storage.getImageResource(userId, oldFileId).orElseThrow();
        var newResourceBundle = storage.getImageResource(userId, newFileId).orElseThrow();

        Assertions.assertThat(oldResourceBundle.meta().fileName()).isEqualTo("old.png");
        Assertions.assertThat(newResourceBundle.meta().fileName()).isEqualTo("new.jpg");
        try (var is = oldResourceBundle.resource().getInputStream()) {
            Assertions.assertThat(is.readAllBytes()).isEqualTo(oldContent);
        }
        try (var is = newResourceBundle.resource().getInputStream()) {
            Assertions.assertThat(is.readAllBytes()).isEqualTo(newContent);
        }
    }

    @Test
    @DisplayName("Returns empty image resource when no image is stored")
    public void givenNoImage_whenImageRequested_thenEmptyResultReturned() {
        var storage = new LocalFileSystemProfileImageStorage(baseFolder);

        Assertions.assertThat(storage.getImageResource(
            BrennonUserId.of("user-1"), FileId.of("file-1"))).isEmpty();
    }
}
