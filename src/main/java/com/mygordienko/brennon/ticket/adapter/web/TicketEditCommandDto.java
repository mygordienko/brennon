package com.mygordienko.brennon.ticket.adapter.web;

import java.util.Collection;
import java.util.List;

import com.mygordienko.brennon.ticket.FileId;
import com.mygordienko.brennon.ticket.TicketAttachmentId;
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
    @Getter
    @Setter
    private Collection<Integer> attachmentIdsToRemove = List.of();
    @Getter
    @Setter
    private Collection<String> uploadedFileIds = List.of();

    public TicketEditCommand toCommand(TicketId ticketId) {
        var attachmentToRemove = attachmentIdsToRemove.stream()
            .map(TicketAttachmentId::of)
            .toList();

        var filesToAdd = uploadedFileIds.stream()
            .map(FileId::of)
            .toList();
        
        return new TicketEditCommand(ticketId, version, subject, text, filesToAdd, attachmentToRemove);
    }
}
