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

        Assertions.assertThat(storage.exists(userId)).isFalse();

        storage.moveToFinal(userId, fileId);

        Assertions.assertThat(storage.exists(userId)).isTrue();

        var resourceBundle = storage.getImageResource(userId).orElseThrow();

        Assertions.assertThat(resourceBundle.meta().fileName()).isEqualTo("profile.png");
        try (var is = resourceBundle.resource().getInputStream()) {
            Assertions.assertThat(is.readAllBytes()).isEqualTo(content);
        }
    }

    @Test
    @DisplayName("Replaces previously stored image")
    public void givenExistingImage_whenNewImageMovedToFinal_thenOldImageIsReplaced() throws Exception {
        var storage = new LocalFileSystemProfileImageStorage(baseFolder);
        var userId = BrennonUserId.of("user-1");
        var oldContent = "old-image".getBytes(StandardCharsets.UTF_8);
        var newContent = "new-image".getBytes(StandardCharsets.UTF_8);

        storage.saveTemp(userId, FileId.of("file-1"), new ByteArrayInputStream(oldContent), "old.png");
        storage.moveToFinal(userId, FileId.of("file-1"));

        storage.saveTemp(userId, FileId.of("file-2"), new ByteArrayInputStream(newContent), "new.jpg");
        storage.moveToFinal(userId, FileId.of("file-2"));

        var resourceBundle = storage.getImageResource(userId).orElseThrow();

        Assertions.assertThat(resourceBundle.meta().fileName()).isEqualTo("new.jpg");
        try (var is = resourceBundle.resource().getInputStream()) {
            Assertions.assertThat(is.readAllBytes()).isEqualTo(newContent);
        }
    }

    @Test
    @DisplayName("Returns empty image resource when no image is stored")
    public void givenNoImage_whenImageRequested_thenEmptyResultReturned() {
        var storage = new LocalFileSystemProfileImageStorage(baseFolder);
        var userId = BrennonUserId.of("user-1");

        Assertions.assertThat(storage.exists(userId)).isFalse();
        Assertions.assertThat(storage.getImageResource(userId)).isEmpty();
    }
}
