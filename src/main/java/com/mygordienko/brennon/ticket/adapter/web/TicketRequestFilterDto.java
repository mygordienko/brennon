package com.mygordienko.brennon.ticket.adapter.web;

import java.util.Optional;

import com.mygordienko.brennon.ticket.TicketId;
import com.mygordienko.brennon.ticket.TicketRequestFilter;
import com.mygordienko.brennon.ticket.TicketStatus;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
public class TicketRequestFilterDto {
    public TicketRequestFilterDto(Long id, String status, String subject) {
        this.id = id;
        this.status = status;
        this.subject = subject;
    }

    @Getter
    @Setter
    @Positive
    private Long id;
    @Getter
    @Setter
    private String status;
    @Getter
    @Setter
    @Size(max = 50)
    private String subject;

    public TicketRequestFilter toDomain() {
        var ticketId = Optional.ofNullable(id).map(TicketId::of).orElse(null);
        var ticketStatus = Optional.ofNullable(status).filter(s -> !s.isBlank()).map(TicketStatus::of).orElse(null);
        return new TicketRequestFilter(ticketId, ticketStatus, subject);
    }
}
