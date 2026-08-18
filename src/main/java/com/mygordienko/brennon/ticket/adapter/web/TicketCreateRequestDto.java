package com.mygordienko.brennon.ticket.adapter.web;

import com.mygordienko.brennon.ticket.TicketCreateRequest;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

public class TicketCreateRequestDto {
    
    public TicketCreateRequestDto() {
    }
    public TicketCreateRequestDto(String subject, String text) {
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

    public TicketCreateRequest toRequest() {
        return new TicketCreateRequest(subject, text);
    }
}
