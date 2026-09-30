package com.mygordienko.brennon.ticket;

import java.util.Collection;

public record TicketCreateCommand(
    String subject,
    String text,
    Collection<FileId> fileIds
) {
    
}
