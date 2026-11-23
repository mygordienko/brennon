package com.mygordienko.brennon.profile.adapter.storage;

import java.io.InputStream;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mygordienko.brennon.core.auth.BrennonUserId;
import com.mygordienko.brennon.core.common.FileId;
import com.mygordienko.brennon.core.common.FileMeta;
import com.mygordienko.brennon.core.common.ResourceBundle;
import com.mygordienko.brennon.profile.ProfileImageStorageException;
import com.mygordienko.brennon.profile.internal.ProfileImageStoragePort;

import io.awspring.cloud.s3.ObjectMetadata;
import io.awspring.cloud.s3.S3Template;
import jakarta.annotation.PostConstruct;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CopyObjectRequest;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;

public class S3ProfileImageStorage implements ProfileImageStoragePort {
    private static final Logger log = LoggerFactory.getLogger("com.mygordienko.brennon.profile.adapter.storage.S3ProfileImageStorage");

    public S3ProfileImageStorage(S3Template s3Template, S3Client s3Client) {
        this.s3Template = s3Template;
        this.s3Client = s3Client;
    }

    @PostConstruct
    public void init() {
        try {
            ensureBucket(TMP_BUCKET);
            ensureBucket(FINAL_BUCKET);
        } catch (Exception e) {
            throw new RuntimeException("S3 profile image storage initialization error", e);
        }
    }

    private void ensureBucket(String bucketName) {
        if (!s3Template.bucketExists(bucketName)) {
            s3Client.createBucket(CreateBucketRequest.builder()
                    .bucket(bucketName)
                    .build());
        } else {
            log.info("Bucket {} already exists.", bucketName);
        }
    }

    private final S3Template s3Template;
    private final S3Client s3Client;

    private static final String TMP_BUCKET = "brennon-profile-tmp";
    private static final String FINAL_BUCKET = "brennon-profile-final";
    private static final String ORIG_FILE_NAME_CUSTOM_ATTRIBUTE = "user-orig-filename";

    @Override
    public void saveTemp(BrennonUserId user, FileId fileId, InputStream is, String fileName) throws ProfileImageStorageException {
        try {
            var metadata = ObjectMetadata.builder()
                    .metadata(ORIG_FILE_NAME_CUSTOM_ATTRIBUTE, fileName)
                    .build();
            s3Template.upload(TMP_BUCKET, getTmpPathForUser(user, fileId), is, metadata);
        } catch (Exception e) {
            throw new ProfileImageStorageException("Error uploading profile image", e);
        }
    }

    @Override
    public void moveToFinal(BrennonUserId user, FileId fileId) throws ProfileImageStorageException {
        try {
            var copyRequest = CopyObjectRequest.builder()
                    .sourceBucket(TMP_BUCKET)
                    .sourceKey(getTmpPathForUser(user, fileId))
                    .destinationBucket(FINAL_BUCKET)
                    .destinationKey(getFinalPathForUser(user, fileId))
                    .build();

            s3Client.copyObject(copyRequest);
        } catch (Exception e) {
            throw new ProfileImageStorageException("Error copying profile image to final storage", e);
        }
    }

    @Override
    public void delete(BrennonUserId user, FileId fileId) throws ProfileImageStorageException {
        try {
            s3Template.deleteObject(FINAL_BUCKET, getFinalPathForUser(user, fileId));
        } catch (Exception e) {
            throw new ProfileImageStorageException("Error deleting profile image", e);
        }
    }

    @Override
    public Optional<ResourceBundle> getImageResource(BrennonUserId user, FileId fileId) throws ProfileImageStorageException {
        try {
            if (!s3Template.objectExists(FINAL_BUCKET, getFinalPathForUser(user, fileId))) {
                return Optional.empty();
            }

            var s3Resource = s3Template.download(FINAL_BUCKET, getFinalPathForUser(user, fileId));
            var fileName = s3Resource.metadata().get(ORIG_FILE_NAME_CUSTOM_ATTRIBUTE);

            return Optional.of(new ResourceBundle(s3Resource, new FileMeta(fileName)));
        } catch (Exception e) {
            throw new ProfileImageStorageException("Error downloading profile image", e);
        }
    }

    private String getTmpPathForUser(BrennonUserId userId, FileId fileId) {
        return "%s/%s".formatted(userId, fileId);
    }

    private String getFinalPathForUser(BrennonUserId userId, FileId fileId) {
        return "%s/%s".formatted(userId, fileId);
    }
}
