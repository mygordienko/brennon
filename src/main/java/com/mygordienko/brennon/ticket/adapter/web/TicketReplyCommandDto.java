package com.mygordienko.brennon.ticket.adapter.web;

import com.mygordienko.brennon.ticket.TicketId;
import com.mygordienko.brennon.ticket.TicketReplyCommand;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

public class TicketReplyCommandDto {
    public TicketReplyCommandDto() {
    }

    public TicketReplyCommandDto(String text) {
        this.text = text;
    }

    @Getter
    @Setter
    @NotBlank
    private String text;

    public TicketReplyCommand toCommand(TicketId ticketId) {
        return new TicketReplyCommand(ticketId, text);
    }
}
