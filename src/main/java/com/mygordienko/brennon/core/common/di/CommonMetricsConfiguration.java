package com.mygordienko.brennon.core.common.di;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import io.micrometer.core.instrument.Clock;
import io.micrometer.core.instrument.logging.LoggingMeterRegistry;
import io.micrometer.core.instrument.logging.LoggingRegistryConfig;

@Configuration 
public class CommonMetricsConfiguration {
    /**
     * Default MeterRegistry. Logs all metrics.
     */
    @Bean
    public LoggingMeterRegistry loggingMeterRegistry(Environment env) {
        LoggingRegistryConfig config = key -> env.getProperty("management.metrics.export.logging." + key);
        return new LoggingMeterRegistry(config, Clock.SYSTEM);
    }
}
