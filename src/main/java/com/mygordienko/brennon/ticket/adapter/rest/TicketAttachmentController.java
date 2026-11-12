package com.mygordienko.brennon.ticket.adapter.rest;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.fileupload2.core.FileItemInput;
import org.apache.commons.fileupload2.core.FileItemInputIterator;
import org.apache.commons.fileupload2.jakarta.servlet6.JakartaServletFileUpload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mygordienko.brennon.ticket.TicketAttachmentId;
import com.mygordienko.brennon.ticket.TicketId;
import com.mygordienko.brennon.ticket.internal.ResourceBundle;
import com.mygordienko.brennon.ticket.internal.TicketAttachmentService;

import jakarta.servlet.http.HttpServletRequest;

@RequestMapping("/api/v1/tickets")
@RestController 
public class TicketAttachmentController {
    public TicketAttachmentController(TicketAttachmentService ticketAttachmentService, FileContentValidator fileContentValidator) {
        this.ticketAttachmentService = ticketAttachmentService;
        this.fileContentValidator = new FileContentValidator();
    }

    private static final Logger log = LoggerFactory.getLogger("com.mygordienko.brennon.ticket.adapter.web.TicketAttachmentController");

    private final TicketAttachmentService ticketAttachmentService;
    private final FileContentValidator fileContentValidator;

    /**
     * NOTE: Custom multipart processing to achieve pure streaming without storing request into temp files.
     */
    @PostMapping("/attachments/upload")
    public UploadedFileDto upload(HttpServletRequest request) {
        var fileId = ticketAttachmentService.gernerateNextFileId();
        try {
            @SuppressWarnings("rawtypes")
            JakartaServletFileUpload upload = new JakartaServletFileUpload();
            FileItemInputIterator iter = upload.getItemIterator(request);

            while (iter.hasNext()) {
                FileItemInput item = iter.next();
                if (!item.isFormField()) {
                    
                    String fileName = item.getName();
                    checkFileName(fileName);

                    try (var is = fileContentValidator.checkRealFileType(item.getInputStream())) {
                        log.info("Uploading file {}", fileName);
                        ticketAttachmentService.saveTemp(fileId, is, fileName);
                    }
                    // NOTE: we expect single file per request
                    break;
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Error uploading file", e);
        }

        return new UploadedFileDto(fileId.toString());
    }

    @GetMapping("/{ticketId}/attachments/{attachmentId}")
    public ResponseEntity<Resource> download(@PathVariable long ticketId, @PathVariable int attachmentId) {
        ResourceBundle bundle = ticketAttachmentService.getAttachmentResource(TicketId.of(ticketId), TicketAttachmentId.of(attachmentId));

        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION,
            "attachment; filename=\"" + bundle.meta().fileName() + "\"").body(bundle.resource());
    }

    private static final Pattern ILLEGAL_FILENAME_CHARACTERS = Pattern.compile("[^a-zA-Z0-9._-]");

    private void checkFileName(String originalFilename) {
        Matcher matcher = ILLEGAL_FILENAME_CHARACTERS.matcher(originalFilename);
        if (matcher.find()) {
            throw new IllegalArgumentException("File to upload has invalid file name");
        }
    }

    public record UploadedFileDto(String fileId) {
    }
}
