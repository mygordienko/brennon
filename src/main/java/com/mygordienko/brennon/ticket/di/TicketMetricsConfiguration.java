package com.mygordienko.brennon.ticket.di;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.mygordienko.brennon.ticket.adapter.metrics.TicketMetricsListener;

import io.micrometer.core.instrument.MeterRegistry;

@Configuration 
public class TicketMetricsConfiguration {
    @Bean 
    public TicketMetricsListener ticketMetricsListener(MeterRegistry meterRegistry) {
        return new TicketMetricsListener(meterRegistry);
    }
}
