package com.mygordienko.brennon.ticket.adapter.storage;

import java.io.InputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mygordienko.brennon.core.auth.BrennonUserId;
import com.mygordienko.brennon.ticket.FileId;
import com.mygordienko.brennon.ticket.FileMeta;
import com.mygordienko.brennon.ticket.TicketAttachmentStorageException;
import com.mygordienko.brennon.ticket.TicketId;
import com.mygordienko.brennon.ticket.internal.ResourceBundle;
import com.mygordienko.brennon.ticket.internal.TicketAttachmentStoragePort;

import io.awspring.cloud.s3.ObjectMetadata;
import io.awspring.cloud.s3.S3Template;
import jakarta.annotation.PostConstruct;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CopyObjectRequest;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;

public class S3TicketAttachmentStorage implements TicketAttachmentStoragePort {
    private static final Logger log = LoggerFactory.getLogger("com.mygordienko.brennon.ticket.adapter.storage.S3TicketAttachmentStorage");
    
    public S3TicketAttachmentStorage(S3Template s3Template, S3Client s3Client) {
        this.s3Template = s3Template;
        this.s3Client = s3Client;
    }

    @PostConstruct
    public void init() {
        try {
            ensureBucket(TMP_BUCKET);
            ensureBucket(FINAL_BUCKET);
        } catch (Exception e) {
            throw new RuntimeException("S3 Storage initialization error", e);
        }
    }

    private void ensureBucket(String bucketName) throws Exception {
        boolean found = s3Template.bucketExists(bucketName);
        if (!found) {
            s3Client.createBucket(CreateBucketRequest.builder()
                    .bucket(bucketName)
                    .build());
        } else {
            log.info("Bucket {} already exists.", bucketName);
        }
    }
    
    private final S3Template s3Template;
    private final S3Client s3Client;

    private static final String TMP_BUCKET = "brennon-tmp";
    private static final String FINAL_BUCKET = "brennon-final";
    private static final String ORIG_FILE_NAME_CUSTOM_ATTRIBUTE = "user-orig-filename";

    @Override
    public void saveTemp(BrennonUserId user, FileId fileId, InputStream is, String fileName) throws TicketAttachmentStorageException {
        try {
            var metadata = ObjectMetadata.builder()
                    .metadata(ORIG_FILE_NAME_CUSTOM_ATTRIBUTE, fileName)
                    .build();
            s3Template.upload(TMP_BUCKET, getTmpPathForUser(user, fileId), is, metadata);
        } catch (Exception e) {
            throw new TicketAttachmentStorageException("Error uploading file", e);
        }
    }

    @Override
    public FileMeta getTempMeta(BrennonUserId user, FileId fileId) throws TicketAttachmentStorageException {
        try {
            var s3Resource = s3Template.download(TMP_BUCKET, getTmpPathForUser(user, fileId));
            var filename = s3Resource.metadata().get(ORIG_FILE_NAME_CUSTOM_ATTRIBUTE);
            return new FileMeta(filename);
        } catch (Exception e) {
            throw new TicketAttachmentStorageException("Error reading temp meta", e);
        }
    }

    @Override
    public void moveToFinal(BrennonUserId user, FileId fileId, TicketId ticketId) throws TicketAttachmentStorageException {
        try {
            CopyObjectRequest copyRequest = CopyObjectRequest.builder()
                    .sourceBucket(TMP_BUCKET)
                    .sourceKey(getTmpPathForUser(user, fileId))
                    .destinationBucket(FINAL_BUCKET)
                    .destinationKey(getFinalPathForTicket(ticketId, fileId))
                    .build();
            
            s3Client.copyObject(copyRequest);
        } catch (Exception e) {
            throw new TicketAttachmentStorageException("Error copying to final", e);
        }
    }

    @Override
    public void delete(FileId fileId, TicketId ticketId) throws TicketAttachmentStorageException {
        try {
            s3Template.deleteObject(FINAL_BUCKET, getFinalPathForTicket(ticketId, fileId));
        } catch (Exception e) {
            throw new TicketAttachmentStorageException("Error deleting object", e);
        }
    }

    @Override
    public ResourceBundle getDownloadResource(FileId fileId, TicketId ticketId) throws TicketAttachmentStorageException {
        try {
            var s3Resource = s3Template.download(FINAL_BUCKET, getFinalPathForTicket(ticketId, fileId));
            var filename = s3Resource.metadata().get(ORIG_FILE_NAME_CUSTOM_ATTRIBUTE);

            return new ResourceBundle(s3Resource, new FileMeta(filename));
        } catch (Exception  e) {
            throw new TicketAttachmentStorageException("Error getting download URL", e);
        }
    }
    
    private String getTmpPathForUser(BrennonUserId userId, FileId fileId) {
        return "%s/%s".formatted(userId, fileId);
    }
    private String getFinalPathForTicket(TicketId ticketId, FileId fileId) {
        return "%s/%s".formatted(ticketId, fileId);
    }
}
