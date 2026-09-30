package com.mygordienko.brennon.ticket.adapter.rest;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.mygordienko.brennon.ticket.TicketAttachmentId;
import com.mygordienko.brennon.ticket.TicketId;
import com.mygordienko.brennon.ticket.internal.ResourceBundle;
import com.mygordienko.brennon.ticket.internal.TicketAttachmentService;

@RequestMapping("/api/v1/tickets")
@RestController 
public class TicketAttachmentController {
    public TicketAttachmentController(TicketAttachmentService ticketAttachmentService) {
        this.ticketAttachmentService = ticketAttachmentService;
    }

    private static final Logger log = LoggerFactory.getLogger("com.mygordienko.brennon.ticket.adapter.web.TicketAttachmentController");

    private final TicketAttachmentService ticketAttachmentService;

    @PostMapping("/attachments/upload")
    public UploadedFileDto upload(
        @RequestParam("file") MultipartFile file
    ) {
        var fileId = ticketAttachmentService.gernerateNextFileId();

        checkFileName(file.getOriginalFilename());

        try (var is = file.getInputStream()) {
            // TODO: file verification
            log.info("Uploading file {}", file.getOriginalFilename());
            ticketAttachmentService.saveTemp(fileId, is, file.getOriginalFilename());
        } catch (IOException e) {
            throw new RuntimeException("Erorr uploading file", e);
        }

        return new UploadedFileDto(fileId.toString());
    }

    @GetMapping("/{ticketId}/attachments/{attachmentId}")
    public ResponseEntity<Resource> download(@PathVariable long ticketId, @PathVariable long attachmentId) {
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
