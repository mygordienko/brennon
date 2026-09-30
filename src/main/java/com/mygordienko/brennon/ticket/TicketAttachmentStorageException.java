package com.mygordienko.brennon.ticket;

public class TicketAttachmentStorageException extends RuntimeException {
    public TicketAttachmentStorageException(Exception e) {
        super(e);
    }

    public TicketAttachmentStorageException(String message) {
        super(message);
    }

    public TicketAttachmentStorageException(String message, Throwable cause) {
        super(message, cause);
    }
}
