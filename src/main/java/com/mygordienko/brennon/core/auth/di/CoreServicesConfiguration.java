package com.mygordienko.brennon.core.auth.di;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import com.mygordienko.brennon.core.auth.BrennonUserProfileService;
import com.mygordienko.brennon.core.auth.BrennonUserProfileStoragePort;
import com.mygordienko.brennon.core.auth.CurrentUserPort;
import com.mygordienko.brennon.core.auth.adapter.jdbc.JdbcBrennonUserProfileStorage;

@Configuration
public class CoreServicesConfiguration {
    @Bean
    public CurrentUserPort currentUserPort() {
        return new CurrentUserPort(){
            //NOTE: intentionally do nothing
        };
    }

    @Bean
    public BrennonUserProfileStoragePort userProfileStoragePort(NamedParameterJdbcTemplate jdbcTemplate) {
        return new JdbcBrennonUserProfileStorage(jdbcTemplate);
    }

    @Bean
    public BrennonUserProfileService userProfileService(
        BrennonUserProfileStoragePort userProfileStoragePort,
        CurrentUserPort currentUserPort) {
        return new BrennonUserProfileService(userProfileStoragePort, currentUserPort);
    }
}
