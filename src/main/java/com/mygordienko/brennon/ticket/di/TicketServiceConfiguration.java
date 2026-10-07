package com.mygordienko.brennon.ticket.di;

import java.net.URI;
import java.nio.file.Paths;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jdbc.core.JdbcAggregateTemplate;
import org.springframework.jdbc.core.simple.JdbcClient;

import com.mygordienko.brennon.core.auth.BrennonUserInfoApi;
import com.mygordienko.brennon.core.auth.internal.BrennonUserProfileService;
import com.mygordienko.brennon.core.common.ClockManager;
import com.mygordienko.brennon.ticket.adapter.jdbc.JdbcTicketStorage;
import com.mygordienko.brennon.ticket.adapter.jdbc.TicketCrudRepository;
import com.mygordienko.brennon.ticket.adapter.storage.LocalFileSystemTicketAttachmentStorage;
import com.mygordienko.brennon.ticket.adapter.storage.S3TicketAttachmentStorage;
import com.mygordienko.brennon.ticket.internal.TicketAttachmentService;
import com.mygordienko.brennon.ticket.internal.TicketAttachmentStoragePort;
import com.mygordienko.brennon.ticket.internal.TicketSecurityManager;
import com.mygordienko.brennon.ticket.internal.TicketService;
import com.mygordienko.brennon.ticket.internal.TicketStoragePort;

import io.awspring.cloud.s3.InMemoryBufferingS3OutputStreamProvider;
import io.awspring.cloud.s3.Jackson2JsonS3ObjectConverter;
import io.awspring.cloud.s3.S3Template;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import tools.jackson.databind.json.JsonMapper;

@Configuration
public class TicketServiceConfiguration {
    @Bean
    public TicketStoragePort ticketStoragePort(JdbcAggregateTemplate jdbcTemplate, JdbcClient jdbcClient, TicketCrudRepository ticketCrudRepository) {
        return new JdbcTicketStorage(jdbcTemplate, jdbcClient, ticketCrudRepository);
    }

    @Bean
    public TicketSecurityManager ticketSecurityManager(BrennonUserInfoApi userProfileService) {
        return new TicketSecurityManager(userProfileService);
    }

    @Bean
    public TicketService ticketService(
            BrennonUserProfileService userProfileService, 
            TicketStoragePort ticketStoragePort, 
            TicketSecurityManager ticketSecurityManager,
            TicketAttachmentStoragePort ticketAttachmentStoragePort,
            ClockManager clockManager) {
        return new TicketService(userProfileService, ticketStoragePort, ticketSecurityManager, ticketAttachmentStoragePort, clockManager);
    }

    @Bean
    @ConditionalOnProperty(prefix = "brennon.ticket.attachments.storage", name = "active", havingValue = "local", matchIfMissing = true)
    public TicketAttachmentStoragePort localTicketAttachmentStoragePort(
            @Value("${brennon.ticket.attachments.storage.local.basefolder}") String basePath) {
        return new LocalFileSystemTicketAttachmentStorage(Paths.get(basePath).toAbsolutePath());
    }



    @Bean
    @ConditionalOnProperty(prefix = "brennon.ticket.attachments.storage", name = "active", havingValue = "s3", matchIfMissing = false)
    public S3Client s3Client(S3TicketAttachmentStorageConfigurationProperties properties) {
        return S3Client.builder()
                .region(Region.of("ru-central1"))
                .forcePathStyle(true)
                .endpointOverride(URI.create(properties.getEndpoint()))
                .credentialsProvider(
                    StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(
                            properties.getCredentials().getAccessKeyId(), 
                            properties.getCredentials().getSecretAccessKey())
                    )
                )
                .build();
    }

    @Bean
    @ConditionalOnProperty(prefix = "brennon.ticket.attachments.storage", name = "active", havingValue = "s3", matchIfMissing = false)
    public S3Presigner s3Presigner(S3TicketAttachmentStorageConfigurationProperties properties) {
        return S3Presigner.builder()
                .region(Region.of("ru-central1"))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(
                            properties.getCredentials().getAccessKeyId(), 
                            properties.getCredentials().getSecretAccessKey())
                ))
                .build();
    }

    @Bean
    @ConditionalOnProperty(prefix = "brennon.ticket.attachments.storage", name = "active", havingValue = "s3", matchIfMissing = false)
    public S3Template s3Template(S3Client s3Client, S3Presigner s3Presigner) {
        InMemoryBufferingS3OutputStreamProvider outputStreamProvider =
                new InMemoryBufferingS3OutputStreamProvider(s3Client, null);

        Jackson2JsonS3ObjectConverter objectConverter =
                new Jackson2JsonS3ObjectConverter(new JsonMapper());

        return new S3Template(s3Client, outputStreamProvider, objectConverter, s3Presigner);
    }

    @Bean
    @ConditionalOnProperty(prefix = "brennon.ticket.attachments.storage", name = "active", havingValue = "s3", matchIfMissing = false)
    public TicketAttachmentStoragePort s3TicketAttachmentStoragePort(S3Template s3Template, S3Client s3Client) {
        return new S3TicketAttachmentStorage(s3Template, s3Client);
    }

    @Bean 
    public TicketAttachmentService ticketAttachmentService(
            TicketStoragePort ticketStoragePort,
            TicketAttachmentStoragePort ticketAttachmentStoragePort, 
            TicketSecurityManager ticketSecurityManager,
            BrennonUserInfoApi userProfileService) {
        return new TicketAttachmentService(ticketStoragePort, ticketAttachmentStoragePort, ticketSecurityManager, userProfileService);
    }
}
