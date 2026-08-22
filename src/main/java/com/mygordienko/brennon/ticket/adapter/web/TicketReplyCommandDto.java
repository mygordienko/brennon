package com.mygordienko.brennon.ticket.adapter.web;

import com.mygordienko.brennon.ticket.TicketId;
import com.mygordienko.brennon.ticket.TicketReplyCommand;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

public class TicketReplyCommandDto {
    public TicketReplyCommandDto() {
    }

    public TicketReplyCommandDto(Integer version) {
        this.version = version;
    }

    public TicketReplyCommandDto(Integer version, String text) {
        this.version = version;
        this.text = text;
    }

    @Getter
    @Setter
    @NotBlank
    private String text;
    @Getter
    @Setter
    @NotNull
    private Integer version;

    public TicketReplyCommand toCommand(TicketId ticketId) {
        return new TicketReplyCommand(ticketId, version, text);
    }
}
