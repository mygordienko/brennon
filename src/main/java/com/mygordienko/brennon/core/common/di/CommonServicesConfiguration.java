package com.mygordienko.brennon.core.common.di;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.mygordienko.brennon.core.common.ClockManager;
import com.mygordienko.brennon.core.common.adapter.SimpleClockManager;

@Configuration
public class CommonServicesConfiguration {
    @Bean
    public ClockManager ClockManager() {
        return new SimpleClockManager();
    }
}
