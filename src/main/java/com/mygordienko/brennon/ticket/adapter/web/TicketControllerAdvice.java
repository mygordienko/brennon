package com.mygordienko.brennon.ticket.adapter.web;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import com.mygordienko.brennon.ticket.MissingTicketException;

@ControllerAdvice
public class TicketControllerAdvice {
    @ExceptionHandler
    public String handleMissingTicket(MissingTicketException ex) {
        return "core/common/404";
    }
}
