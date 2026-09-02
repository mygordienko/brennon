package com.mygordienko.brennon.ticket.adapter.web;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.mygordienko.brennon.ticket.MissingTicketException;
import com.mygordienko.brennon.ticket.MissingTicketReplyException;

@ControllerAdvice
public class TicketControllerAdvice {
    @ExceptionHandler({MissingTicketException.class, MissingTicketReplyException.class})
    @ResponseStatus(code = HttpStatus.NOT_FOUND)
    public String handleMissingTicket(Exception ex) {
        return "core/common/404";
    }

    @ExceptionHandler
    @ResponseStatus(code = HttpStatus.BAD_REQUEST)
    public String handleBadRequest(IllegalArgumentException ex) {
        return "core/common/400";
    }

    @ExceptionHandler
    @ResponseStatus(code = HttpStatus.INTERNAL_SERVER_ERROR)
    public String handleOtherErrors(Exception ex) {
        return "core/common/500";
    }
}
