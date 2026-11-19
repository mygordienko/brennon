package com.mygordienko.brennon.ticket;

import java.util.Collection;

import com.mygordienko.brennon.core.common.FileId;

public record TicketCreateCommand(
    String subject,
    String text,
    Collection<FileId> fileIds
) {
    
}
