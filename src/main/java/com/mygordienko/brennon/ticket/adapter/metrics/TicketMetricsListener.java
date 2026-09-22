package com.mygordienko.brennon.ticket.adapter.metrics;

import java.time.Duration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;

import com.mygordienko.brennon.ticket.TicketClosedEvent;
import com.mygordienko.brennon.ticket.TicketCreatedEvent;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;

public class TicketMetricsListener {
    public TicketMetricsListener(MeterRegistry meterRegistry) {
        this.ticketCreatedCounter = Counter.builder(TicketMetrics.TICKETS_CREATED.getCode())
            .description(TicketMetrics.TICKETS_CREATED.getDescription())
            .register(meterRegistry);

        this.ticketResolutionTimer = Timer.builder(TicketMetrics.TICKET_RESOLUTION_TIME.getCode())
            .description(TicketMetrics.TICKET_RESOLUTION_TIME.getDescription())
            .register(meterRegistry);
    }

    private static final Logger log = LoggerFactory.getLogger(TicketMetricsListener.class);

    private final Counter ticketCreatedCounter;
    private final Timer ticketResolutionTimer;

    @EventListener
    public void handleTicketCreated(TicketCreatedEvent event) {
        log.debug("Ticket metrics handle TicketCreatedEvent: %s".formatted(event));
        ticketCreatedCounter.increment();
    }

    @EventListener
    public void handleTicketClosed(TicketClosedEvent event) {
        log.debug("Ticket metrics handle TicketClosedEvent: %s".formatted(event));
        Duration resolutionDuration = Duration.between(event.createdAt(), event.closedAt());
        ticketResolutionTimer.record(resolutionDuration);
    }
} 
