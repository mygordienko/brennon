package com.mygordienko.brennon.ticket.di;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

@ConfigurationProperties(prefix = "brennon.ticket.attachments.storage.s3")
public class S3TicketAttachmentStorageConfigurationProperties {
    private String endpoint;
    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    @NestedConfigurationProperty
    private Credentials credentials;
    public Credentials getCredentials() {
        return credentials;
    }

    public void setCredentials(Credentials credentials) {
        this.credentials = credentials;
    }

    public static class Credentials {
        private String accessKeyId;
        public String getAccessKeyId() {
            return accessKeyId;
        }
        public void setAccessKeyId(String accessKeyId) {
            this.accessKeyId = accessKeyId;
        }

        private String secretAccessKey;
        public String getSecretAccessKey() {
            return secretAccessKey;
        }
        public void setSecretAccessKey(String secretAccessKey) {
            this.secretAccessKey = secretAccessKey;
        }
    }
}
