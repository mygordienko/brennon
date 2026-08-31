package com.mygordienko.brennon.ticket;

import org.jspecify.annotations.Nullable;

public record TicketRequestFilter(
    @Nullable TicketStatus status
) {
}
