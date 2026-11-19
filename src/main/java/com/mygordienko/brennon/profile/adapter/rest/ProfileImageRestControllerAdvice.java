package com.mygordienko.brennon.profile.adapter.rest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Order(100)
@RestControllerAdvice(basePackageClasses = {ProfileImageRestController.class})
public class ProfileImageRestControllerAdvice {
    private static final Logger log = LoggerFactory.getLogger("com.mygordienko.brennon.profile.adapter.rest.ProfileImageRestControllerAdvice");

    @ExceptionHandler
    @ResponseStatus(code = HttpStatus.BAD_REQUEST)
    public ProblemDetail handleBadProfileImage(IllegalArgumentException ex) {
        log.error("Bad profile image", ex);
        return ProblemDetail.forStatusAndDetail(HttpStatusCode.valueOf(400), "Unsupported profile image");
    }

    @ExceptionHandler
    @ResponseStatus(code = HttpStatus.INTERNAL_SERVER_ERROR)
    public ProblemDetail handleUnexpectedError(Exception ex) {
        log.error("Unexpected error during profile image REST processing", ex);
        return ProblemDetail.forStatusAndDetail(HttpStatusCode.valueOf(500), "Internal server error");
    }
}
