package com.mygordienko.brennon.ticket.adapter.web;

import java.util.Collection;
import java.util.List;

import com.mygordienko.brennon.ticket.FileId;
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
    @Getter
    @Setter
    private Collection<String> uploadedFileIds = List.of();

    public TicketCreateCommand toCommand() {
        var files = uploadedFileIds.stream()
            .map(FileId::of)
            .toList();
        return new TicketCreateCommand(subject, text, files);
    }
}
