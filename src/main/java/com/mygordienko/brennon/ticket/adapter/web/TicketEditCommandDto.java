package com.mygordienko.brennon.ticket.adapter.web;

import com.mygordienko.brennon.ticket.TicketEditCommand;
import com.mygordienko.brennon.ticket.TicketId;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

public class TicketEditCommandDto {
    public TicketEditCommandDto() {
    }
    public TicketEditCommandDto(Integer version, String subject, String text) {
        this.version = version;
        this.subject = subject;
        this.text = text;
    }

    @Getter
    @Setter
    @NotNull
    private Integer version;
    @Getter
    @Setter
    @NotBlank
    private String subject;
    @Getter
    @Setter
    @NotBlank
    private String text;

    public TicketEditCommand toCommand(TicketId ticketId) {
        return new TicketEditCommand(ticketId, version, subject, text);
    }
}
