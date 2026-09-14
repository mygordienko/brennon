package com.mygordienko.brennon.ticket.adapter.web;

import com.mygordienko.brennon.ticket.TicketCloseCommand;
import com.mygordienko.brennon.ticket.TicketId;

import jakarta.validation.constraints.NotNull;

public class TicketCloseCommandDto {
    public TicketCloseCommandDto() {
    }
    public TicketCloseCommandDto(Integer version) {
        this.version = version;
    }

    @NotNull
    private Integer version;

    public Integer getVersion() {
        return version;
    }
    public void setVersion(Integer version) {
        this.version = version;
    }

    public TicketCloseCommand toCommand(TicketId ticketId) {
        return new TicketCloseCommand(ticketId, version);
    }
    
}
