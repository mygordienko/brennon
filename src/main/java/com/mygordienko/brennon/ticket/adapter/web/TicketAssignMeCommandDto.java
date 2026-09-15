package com.mygordienko.brennon.ticket.adapter.web;

import com.mygordienko.brennon.ticket.TicketAssignMeCommand;
import com.mygordienko.brennon.ticket.TicketId;

import jakarta.validation.constraints.NotNull;

public class TicketAssignMeCommandDto {
    public TicketAssignMeCommandDto() {
    }
    public TicketAssignMeCommandDto(Integer version) {
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

    public TicketAssignMeCommand toCommand(TicketId ticketId) {
        return new TicketAssignMeCommand(ticketId, version);
    }
}
