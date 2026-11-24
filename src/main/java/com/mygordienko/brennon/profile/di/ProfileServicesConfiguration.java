package com.mygordienko.brennon.profile.di;

import java.net.URI;
import java.nio.file.Paths;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jdbc.core.JdbcAggregateTemplate;

import com.mygordienko.brennon.core.auth.BrennonUserAccountApi;
import com.mygordienko.brennon.core.common.ClockManager;
import com.mygordienko.brennon.profile.adapter.jdbc.BrennonUserProfileCrudRepository;
import com.mygordienko.brennon.profile.adapter.jdbc.JdbcBrennonUserProfileStorage;
import com.mygordienko.brennon.profile.adapter.rest.ProfileImageUrlResolver;
import com.mygordienko.brennon.profile.adapter.storage.LocalFileSystemProfileImageStorage;
import com.mygordienko.brennon.profile.adapter.storage.S3ProfileImageStorage;
import com.mygordienko.brennon.profile.internal.BrennonUserProfilePort;
import com.mygordienko.brennon.profile.internal.BrennonUserProfileService;
import com.mygordienko.brennon.profile.internal.ProfileImageService;
import com.mygordienko.brennon.profile.internal.ProfileImageStoragePort;

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
public class ProfileServicesConfiguration {
    @Bean
    public JdbcBrennonUserProfileStorage userProfileStoragePort(
            JdbcAggregateTemplate jdbcTemplate,
            BrennonUserProfileCrudRepository springRepository) {
        return new JdbcBrennonUserProfileStorage(jdbcTemplate, springRepository);
    }

    @Bean
    public BrennonUserProfileService userProfileService(
        BrennonUserProfilePort userProfileStoragePort,
        BrennonUserAccountApi userAccountApi) {
        return new BrennonUserProfileService(userProfileStoragePort, userAccountApi);
    }

    @Bean
    public ProfileImageUrlResolver profileImageUrlResolver() {
        return new ProfileImageUrlResolver();
    }

    @Bean
    public ProfileImageService profileImageService(
            ProfileImageStoragePort profileImageStoragePort,
            BrennonUserProfileService userProfileService,
            ClockManager clockManager) {
        return new ProfileImageService(profileImageStoragePort, userProfileService, clockManager);
    }

    @Bean
    @ConditionalOnProperty(prefix = "brennon.profile.images.storage", name = "active", havingValue = "local", matchIfMissing = true)
    public ProfileImageStoragePort localProfileImageStoragePort(
            @Value("${brennon.profile.images.storage.local.basefolder}") String basePath) {
        return new LocalFileSystemProfileImageStorage(Paths.get(basePath).toAbsolutePath());
    }

    @Bean
    @ConditionalOnProperty(prefix = "brennon.profile.images.storage", name = "active", havingValue = "s3", matchIfMissing = false)
    public S3Client profileS3Client(S3ProfileImageStorageConfigurationProperties properties) {
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
    @ConditionalOnProperty(prefix = "brennon.profile.images.storage", name = "active", havingValue = "s3", matchIfMissing = false)
    public S3Presigner profileS3Presigner(S3ProfileImageStorageConfigurationProperties properties) {
        return S3Presigner.builder()
                .region(Region.of("ru-central1"))
                .endpointOverride(URI.create(properties.getEndpoint()))
                .credentialsProvider(StaticCredentialsProvider.create(
                    AwsBasicCredentials.create(
                        properties.getCredentials().getAccessKeyId(),
                        properties.getCredentials().getSecretAccessKey())
                ))
                .build();
    }

    @Bean
    @ConditionalOnProperty(prefix = "brennon.profile.images.storage", name = "active", havingValue = "s3", matchIfMissing = false)
    public S3Template profileS3Template(
            @Qualifier("profileS3Client") S3Client profileS3Client,
            @Qualifier("profileS3Presigner") S3Presigner profileS3Presigner) {
        var outputStreamProvider = new InMemoryBufferingS3OutputStreamProvider(profileS3Client, null);
        var objectConverter = new Jackson2JsonS3ObjectConverter(new JsonMapper());

        return new S3Template(profileS3Client, outputStreamProvider, objectConverter, profileS3Presigner);
    }

    @Bean
    @ConditionalOnProperty(prefix = "brennon.profile.images.storage", name = "active", havingValue = "s3", matchIfMissing = false)
    public ProfileImageStoragePort s3ProfileImageStoragePort(
            @Qualifier("profileS3Template") S3Template profileS3Template,
            @Qualifier("profileS3Client") S3Client profileS3Client) {
        return new S3ProfileImageStorage(profileS3Template, profileS3Client);
    }
}
