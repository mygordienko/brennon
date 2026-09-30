package com.mygordienko.brennon.ticket.internal;

import java.io.InputStream;

import com.mygordienko.brennon.core.auth.BrennonUserId;
import com.mygordienko.brennon.ticket.FileId;
import com.mygordienko.brennon.ticket.FileMeta;
import com.mygordienko.brennon.ticket.TicketAttachmentStorageException;
import com.mygordienko.brennon.ticket.TicketId;

public interface TicketAttachmentStoragePort {
    void saveTemp(BrennonUserId user, FileId fileId, InputStream is, String fileName) throws TicketAttachmentStorageException;
    FileMeta getTempMeta(BrennonUserId user, FileId fileId) throws TicketAttachmentStorageException;
    void moveToFinal(BrennonUserId user, FileId fileId, TicketId ticketId) throws TicketAttachmentStorageException;
    void delete(FileId fileId, TicketId ticketId) throws TicketAttachmentStorageException;
    ResourceBundle getDownloadResource(FileId fileId, TicketId ticketId) throws TicketAttachmentStorageException;
}
