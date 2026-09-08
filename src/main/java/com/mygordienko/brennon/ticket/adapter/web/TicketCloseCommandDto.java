package com.mygordienko.brennon.ticket.adapter.web;

import com.mygordienko.brennon.ticket.TicketCloseCommand;
import com.mygordienko.brennon.ticket.TicketId;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

public class TicketCloseCommandDto {
    public TicketCloseCommandDto() {
    }
    public TicketCloseCommandDto(Integer version) {
        this.version = version;
    }

    @Getter
    @Setter
    @NotNull
    private Integer version;

    public TicketCloseCommand toCommand(TicketId ticketId) {
        return new TicketCloseCommand(ticketId, version);
    }
}
