package com.mygordienko.brennon.ticket.adapter.web;

import java.util.Optional;

import org.springframework.context.MessageSource;
import org.springframework.core.annotation.Order;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.ModelAndView;

import com.mygordienko.brennon.ticket.MissingTicketException;
import com.mygordienko.brennon.ticket.MissingTicketReplyException;
import com.mygordienko.brennon.ticket.TicketConcurrentModificationException;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

/**
 * This ControllerAdvice is used to handle errors coming from both HTMX and regular requests.
 * 
 * NOTE: We can not use `@ResponseStatus` annotation on handler methods because it forces normal view resolution flow
 *  and we want to have mixed responses:
 *      - view name for regular requests
 *      - JSON for HTMX
 */
@Slf4j
@Order(200)
@ControllerAdvice(basePackages = {"com.mygordienko.brennon.ticket.adapter.web"})
public class TicketControllerAdvice {
    public TicketControllerAdvice(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    private final MessageSource messageSource;

    @ExceptionHandler({MissingTicketException.class, MissingTicketReplyException.class})
    @ResponseStatus(code = HttpStatus.NOT_FOUND)
    public String handleMissingTicket(Exception ex) {
        log.error("Missing error", ex);
        return "core/common/404";
    }

    @ExceptionHandler
    @ResponseStatus(code = HttpStatus.BAD_REQUEST)
    public String handleBadRequest(IllegalArgumentException ex) {
        log.error("Illegal argument error", ex);
        return "core/common/400";
    }

    @ExceptionHandler
    @ResponseStatus(code = HttpStatus.BAD_REQUEST)
    public String handleBadRequest(IllegalStateException ex) {
        log.error("Illegal state error", ex);
        return "core/common/400";
    }

    @ExceptionHandler
    public Object handleOptimisticLock(
        HttpServletRequest request,
        OptimisticLockingFailureException ex) {
        log.error("Concurrent modification exception", ex);

        var htmx = Optional.ofNullable(request.getHeader("HX-Request")).isPresent();

        if (htmx) {
            var message = messageSource.getMessage("ticket.error.concurrent", null, null);
            return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, message));
        }
        
        return new ModelAndView("core/common/400", HttpStatus.CONFLICT);
    }

    @ExceptionHandler
    public Object handleCustomConcurrentException(
        HttpServletRequest request,
        TicketConcurrentModificationException ex) {
        log.error("Concurrent modification exception", ex);

        var htmx = Optional.ofNullable(request.getHeader("HX-Request")).isPresent();

        if (htmx) {
            var message = messageSource.getMessage("ticket.error.concurrent", null, null);
            return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, message));
        }

        return new ModelAndView("core/common/400", HttpStatus.CONFLICT);
    }


}