package com.mygordienko.brennon.profile.di;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jdbc.core.JdbcAggregateTemplate;

import com.mygordienko.brennon.core.auth.BrennonUserAccountApi;
import com.mygordienko.brennon.profile.adapter.jdbc.JdbcBrennonUserProfileStorage;
import com.mygordienko.brennon.profile.internal.BrennonUserProfilePort;
import com.mygordienko.brennon.profile.internal.BrennonUserProfileService;

@Configuration
public class ProfileServicesConfiguration {
    @Bean
    public JdbcBrennonUserProfileStorage userProfileStoragePort(JdbcAggregateTemplate jdbcTemplate) {
        return new JdbcBrennonUserProfileStorage(jdbcTemplate);
    }

    @Bean
    public BrennonUserProfileService userProfileService(
        BrennonUserProfilePort userProfileStoragePort,
        BrennonUserAccountApi userAccountApi) {
        return new BrennonUserProfileService(userProfileStoragePort, userAccountApi);
    }
}
