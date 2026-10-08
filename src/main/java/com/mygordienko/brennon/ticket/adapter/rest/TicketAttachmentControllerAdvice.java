package com.mygordienko.brennon.ticket.adapter.rest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.mygordienko.brennon.ticket.MissingTicketException;

@Order(100)
@RestControllerAdvice(basePackageClasses = {TicketAttachmentController.class})
public class TicketAttachmentControllerAdvice {
    private static final Logger log = LoggerFactory.getLogger("com.mygordienko.brennon.ticket.adapter.rest.TicketAttachmentControllerAdvice");
    
    @ExceptionHandler
    @ResponseStatus(code = HttpStatus.BAD_REQUEST)
    public ProblemDetail handleBadAttachment(IllegalArgumentException ex) {
        log.error("Bad attachment", ex);
        return ProblemDetail.forStatusAndDetail(HttpStatusCode.valueOf(400), "Unsupported attachment");
    }

    @ExceptionHandler
    @ResponseStatus(code = HttpStatus.NOT_FOUND)
    public ProblemDetail handleMissingTicket(MissingTicketException ex) {
        log.error("Missing ticket during REST processing", ex);
        return ProblemDetail.forStatusAndDetail(HttpStatusCode.valueOf(404), "Ticket not found");
    }

    @ExceptionHandler
    @ResponseStatus(code = HttpStatus.INTERNAL_SERVER_ERROR)
    public ProblemDetail handleUnexpectedError(Exception ex) {
        log.error("Unexpected error during REST processing", ex);
        return ProblemDetail.forStatusAndDetail(HttpStatusCode.valueOf(500), "Internal server error");
    }
}
