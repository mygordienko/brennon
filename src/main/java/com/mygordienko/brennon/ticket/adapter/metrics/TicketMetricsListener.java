package com.mygordienko.brennon.ticket.adapter.metrics;

import java.time.Duration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;

import com.mygordienko.brennon.ticket.TicketClosedEvent;
import com.mygordienko.brennon.ticket.TicketCreatedEvent;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.MeterRegistry;

public class TicketMetricsListener {
    public TicketMetricsListener(MeterRegistry meterRegistry) {
        this.ticketCreatedCounter = Counter.builder(TicketMetrics.TICKETS_CREATED.getCode())
            .description(TicketMetrics.TICKETS_CREATED.getDescription())
            .register(meterRegistry);

        // NOTE: ticket resolution can take days, we can not use Timer or LongTaskTimer
        this.ticketResolutionDistribution = DistributionSummary.builder(TicketMetrics.TICKET_RESOLUTION_TIME.getCode())
                .description(TicketMetrics.TICKET_RESOLUTION_TIME.getDescription())
                .baseUnit("hours")
                .serviceLevelObjectives(1.0, 4.0, 12.0, 24.0, 48.0, 72.0)
                .register(meterRegistry);
    }

    private static final Logger log = LoggerFactory.getLogger(TicketMetricsListener.class);

    private final Counter ticketCreatedCounter;
    private final DistributionSummary ticketResolutionDistribution;

    @EventListener
    public void handleTicketCreated(TicketCreatedEvent event) {
        log.debug("Ticket metrics handle TicketCreatedEvent: %s".formatted(event));
        ticketCreatedCounter.increment();
    }

    @EventListener
    public void handleTicketClosed(TicketClosedEvent event) {
        log.debug("Ticket metrics handle TicketClosedEvent: %s".formatted(event));
        Long resolutionDuration = Duration.between(event.createdAt(), event.closedAt()).toHours();
        ticketResolutionDistribution.record(resolutionDuration);
    }
} 
