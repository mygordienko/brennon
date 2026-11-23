package com.mygordienko.brennon.profile.adapter.web;

import java.util.Optional;

import org.springframework.context.MessageSource;
import org.springframework.core.annotation.Order;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;

import com.mygordienko.brennon.profile.ProfileConcurrentModificationException;

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
@ControllerAdvice(basePackages = {"com.mygordienko.brennon.profile.adapter.web"})
public class ProfileControllerAdvice {
    public ProfileControllerAdvice(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    private final MessageSource messageSource;

    @ExceptionHandler
    public Object handleOptimisticLock(
        HttpServletRequest request,
        OptimisticLockingFailureException ex) {
        log.error("Concurrent profile modification exception", ex);

        return concurrentModificationResponse(request);
    }

    @ExceptionHandler
    public Object handleCustomConcurrentException(
        HttpServletRequest request,
        ProfileConcurrentModificationException ex) {
        log.error("Concurrent profile modification exception", ex);

        return concurrentModificationResponse(request);
    }

    private Object concurrentModificationResponse(HttpServletRequest request) {
        var htmx = Optional.ofNullable(request.getHeader("HX-Request")).isPresent();

        if (htmx) {
            var message = messageSource.getMessage("profile.error.concurrent", null, null);
            return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, message));
        }

        return new ModelAndView("core/common/400", HttpStatus.CONFLICT);
    }
}
