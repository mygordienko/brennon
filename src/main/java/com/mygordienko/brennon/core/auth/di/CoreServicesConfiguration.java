package com.mygordienko.brennon.core.auth.di;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jdbc.core.JdbcAggregateTemplate;

import com.mygordienko.brennon.core.auth.BrennonUserPort;
import com.mygordienko.brennon.core.auth.BrennonUserProfileService;
import com.mygordienko.brennon.core.auth.BrennonUserProfileStoragePort;
import com.mygordienko.brennon.core.auth.adapter.jdbc.JdbcBrennonUserProfileStorage;

@Configuration
public class CoreServicesConfiguration {
    @Bean
    public JdbcBrennonUserProfileStorage userProfileStoragePort(JdbcAggregateTemplate jdbcTemplate) {
        return new JdbcBrennonUserProfileStorage(jdbcTemplate);
    }

    @Bean
    public BrennonUserProfileService userProfileService(
        BrennonUserProfileStoragePort userProfileStoragePort,
        BrennonUserPort currentUserPort) {
        return new BrennonUserProfileService(userProfileStoragePort, currentUserPort);
    }
}
