package com.mygordienko.brennon.ticket.adapter.web;

import com.mygordienko.brennon.ticket.TicketEditReplyCommand;
import com.mygordienko.brennon.ticket.TicketId;
import com.mygordienko.brennon.ticket.TicketReplyId;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

public class TicketEditReplyCommandDto {
    public TicketEditReplyCommandDto() {
    }

    public TicketEditReplyCommandDto(Integer version, Integer replyId, String text) {
        this.version = version;
        this.replyId = replyId;
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
    @Getter
    @Setter
    private Integer replyId;

    public TicketEditReplyCommand toCommand(TicketId ticketId) {
        return new TicketEditReplyCommand(ticketId, version, TicketReplyId.of(replyId), text);
    }
}
