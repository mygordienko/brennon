package com.mygordienko.brennon.ticket;

import org.jspecify.annotations.Nullable;

public record TicketRequestFilter(
    @Nullable TicketId id,
    @Nullable TicketStatus status,
    @Nullable String subject
) {
}
