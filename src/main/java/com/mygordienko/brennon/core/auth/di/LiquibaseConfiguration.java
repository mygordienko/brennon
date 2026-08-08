package com.mygordienko.brennon.core.auth.di;

import javax.sql.DataSource;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import liquibase.integration.spring.SpringLiquibase;

@Configuration
public class LiquibaseConfiguration {
    @Bean
    public SpringLiquibase liquibase(DataSource dataSource) {
        final var liquibase = new SpringLiquibase();
        // NOTE: disable, there is an existing schema (HRBO)
        liquibase.setShouldRun(true);
        liquibase.setDataSource(dataSource);
        liquibase.setChangeLog("classpath:db/changelog/main-changelog.yaml");
        liquibase.setDefaultSchema("brennon");
        liquibase.setLiquibaseSchema("brennon");

        return liquibase;
    }
}
