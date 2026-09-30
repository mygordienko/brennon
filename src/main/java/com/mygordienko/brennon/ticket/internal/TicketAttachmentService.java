package com.mygordienko.brennon.ticket.internal;

import java.io.InputStream;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.security.access.prepost.PreAuthorize;

import com.mygordienko.brennon.core.auth.BrennonUserInfoApi;
import com.mygordienko.brennon.ticket.FileId;
import com.mygordienko.brennon.ticket.MissingTicketAttachmentException;
import com.mygordienko.brennon.ticket.MissingTicketException;
import com.mygordienko.brennon.ticket.TicketAttachmentAddedEvent;
import com.mygordienko.brennon.ticket.TicketAttachmentId;
import com.mygordienko.brennon.ticket.TicketAttachmentRemovedEvent;
import com.mygordienko.brennon.ticket.TicketId;

public class TicketAttachmentService {
    public TicketAttachmentService(
            TicketStoragePort ticketStoragePort,
            TicketAttachmentStoragePort ticketAttachmentStoragePort, 
            TicketSecurityManager ticketSecurityManager,
            BrennonUserInfoApi userProfileService) {
        this.ticketStoragePort = ticketStoragePort;
        this.ticketAttachmentStoragePort = ticketAttachmentStoragePort;
        this.ticketSecurityManager = ticketSecurityManager;
        this.userProfileService = userProfileService;
    }

    private static final Logger log = LoggerFactory.getLogger(TicketAttachmentService.class);

    private final TicketStoragePort ticketStoragePort;
    private final TicketAttachmentStoragePort ticketAttachmentStoragePort;
    private final TicketSecurityManager ticketSecurityManager;
    private final BrennonUserInfoApi userProfileService;

    @PreAuthorize("hasAuthority('user:write')")
    public FileId gernerateNextFileId() {
        return FileId.of(UUID.randomUUID().toString());
    }

    @PreAuthorize("hasAuthority('user:write')")
    public void saveTemp(FileId fileId, InputStream is, String fileName) {
        var currentUser = userProfileService.getCurrentUser();
        ticketAttachmentStoragePort.saveTemp(currentUser.getId(), fileId, is, fileName);
    }

    /**
     * TODO: make transactional and async when Messaging Middleware is ready. 
     *  That might require the event to provide user ID.
     */
    @EventListener 
    public void processAttachmentAdd(TicketAttachmentAddedEvent event) {
        log.debug("processAttachmentAdd ticket: {} attachment: {}", event.ticketId(), event.attachmentId());
        var currentUser = userProfileService.getCurrentUser();
        var ticket = ticketStoragePort.getTicketById(event.ticketId()).orElseThrow(() -> MissingTicketException.forTicket(event.ticketId()));
        var attachment = ticket.getAttachments().stream()
            .filter(att -> att.getId().equals(event.attachmentId()))
            .findFirst()
            .orElseThrow(() -> MissingTicketAttachmentException.forTicket(ticket.getId(), event.attachmentId()));

        ticketAttachmentStoragePort.moveToFinal(currentUser.getId(), attachment.getFileId(), ticket.getId());
    }

    /**
     * TODO: make transactional and async when Messaging Middleware is ready
     */
    @EventListener 
    public void processAttachmentRemoved(TicketAttachmentRemovedEvent event) {
        log.debug("processAttachmentRemoved ticket: {} attachment: {} file {}", event.ticketId(), event.attachmentId(), event.fileId());
        ticketAttachmentStoragePort.delete(event.fileId(), event.ticketId());
    }

    @PreAuthorize("hasAuthority('user:read')")
    public ResourceBundle getAttachmentResource(TicketId ticketId, TicketAttachmentId attachmentId) {
        var currentUser = userProfileService.getCurrentUser();
        var ticket = ticketStoragePort.getTicketById(ticketId)
            .filter(t -> ticketSecurityManager.userCanRead(currentUser, t))
            .orElseThrow(() -> MissingTicketException.forTicket(ticketId));

        var attachment = ticket.getAttachments().stream()
            .filter(att -> att.getId().equals(attachmentId))
            .findFirst()
            .orElseThrow(() -> MissingTicketAttachmentException.forTicket(ticket.getId(), attachmentId));

        return ticketAttachmentStoragePort.getDownloadResource(attachment.getFileId(), ticketId);
    }
}
