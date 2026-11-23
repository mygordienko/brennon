package com.mygordienko.brennon.profile.adapter.jdbc;

import java.time.OffsetDateTime;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jdbc.test.autoconfigure.DataJdbcTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.jdbc.core.JdbcAggregateTemplate;
import org.springframework.test.context.event.ApplicationEvents;
import org.springframework.test.context.event.RecordApplicationEvents;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

import com.mygordienko.brennon.core.auth.BrennonUserId;
import com.mygordienko.brennon.core.auth.di.BrennonUserDataJdbcConfiguration;
import com.mygordienko.brennon.core.common.FileId;
import com.mygordienko.brennon.core.common.di.CommonDataJdbcConfiguration;
import com.mygordienko.brennon.core.common.di.LiquibaseConfiguration;
import com.mygordienko.brennon.profile.BrennonUserProfile;
import com.mygordienko.brennon.profile.ProfileImageUpdatedEvent;

@DataJdbcTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
@RecordApplicationEvents
@Import({LiquibaseConfiguration.class, CommonDataJdbcConfiguration.class, BrennonUserDataJdbcConfiguration.class})
public class JdbcBrennonUserProfileStorageTest {

    @Autowired
    private JdbcAggregateTemplate jdbcAggregateTemplate;

    @Autowired
    private BrennonUserProfileCrudRepository springRepository;

    @Autowired
    private ApplicationEvents applicationEvents;

    @Test
    @Transactional
    @DisplayName("Bumps aggregate version when profile image is updated")
    public void givenExistingProfile_whenProfileImageUpdated_thenVersionIsBumped() {
        var storage = new JdbcBrennonUserProfileStorage(jdbcAggregateTemplate, springRepository);
        var profile = storage.getById(BrennonUserId.of("1")).orElseThrow();
        var fileId = FileId.of("file-1");
        var updated = OffsetDateTime.parse("2026-08-30T15:35:00+02:00");

        Assertions.assertThat(profile.getVersion()).isZero();
        Assertions.assertThat(profile.getProfileImageFileId()).isEmpty();
        Assertions.assertThat(profile.getCreated()).isNotNull();
        Assertions.assertThat(profile.getUpdated()).isNotNull();

        profile.updateProfileImage(profile.getId(), fileId, updated);
        storage.save(profile);

        var updatedProfile = storage.getById(profile.getId()).orElseThrow();

        Assertions.assertThat(updatedProfile.getVersion()).isEqualTo(1);
        Assertions.assertThat(updatedProfile.getProfileImageFileId()).contains(fileId);
        Assertions.assertThat(updatedProfile.getUpdated()).isEqualTo(updated);

        var events = applicationEvents.stream(ProfileImageUpdatedEvent.class).toList();
        Assertions.assertThat(events).hasSize(1);
        Assertions.assertThat(events.getFirst().profileId()).isEqualTo(profile.getId());
        Assertions.assertThat(events.getFirst().previousFileId()).isNull();
        Assertions.assertThat(events.getFirst().newFileId()).isEqualTo(fileId);
    }

    @Test
    @Transactional
    @DisplayName("Rejects save based on a stale aggregate version")
    public void givenStaleProfile_whenSaved_thenOptimisticLockFailureReported() {
        var storage = new JdbcBrennonUserProfileStorage(jdbcAggregateTemplate, springRepository);
        var profile = storage.getById(BrennonUserId.of("1")).orElseThrow();
        var staleProfile = storage.getById(BrennonUserId.of("1")).orElseThrow();
        var updated = OffsetDateTime.parse("2026-08-30T15:35:00+02:00");

        profile.updateProfileImage(profile.getId(), FileId.of("file-1"), updated);
        storage.save(profile);

        staleProfile.updateProfileImage(staleProfile.getId(), FileId.of("file-2"), updated);

        Assertions.assertThatThrownBy(() -> storage.save(staleProfile))
            .isInstanceOf(OptimisticLockingFailureException.class);
    }

    @TestConfiguration
    public static class DbTestConfiguration {
        @Bean
        @ServiceConnection
        public PostgreSQLContainer postreSqlContainer() {
            PostgreSQLContainer postgreSQLContainer = new PostgreSQLContainer(DockerImageName.parse("postgres:18.6"))
                .withDatabaseName("brennon")
                .withUsername("postgres")
                .withPassword("example")
                .withUrlParam("currentSchema", "brennon")
                .withInitScript("init.sql");
            return postgreSQLContainer;
        }
    }
}
