package com.mygordienko.brennon.ticket.adapter.web;

import com.mygordienko.brennon.ticket.TicketCreateCommand;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

public class TicketCreateCommandDto {
    
    public TicketCreateCommandDto() {
    }
    public TicketCreateCommandDto(String subject, String text) {
        this.subject = subject;
        this.text = text;
    }

    @Getter
    @Setter
    @NotBlank
    private String subject;
    @Getter
    @Setter
    @NotBlank
    private String text;

    public TicketCreateCommand toCommand() {
        return new TicketCreateCommand(subject, text);
    }
}
