package com.mygordienko.brennon.core.common.adapter;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import jakarta.servlet.http.HttpServletResponse;

/**
 * NOTE: response.sendError() is required to engage custom `CustomErrorPageController`.
 *   Using @ResponseStatus alone is not enough because @ResponseStatus only modifies the HTTP status code 
 *   on the current response header, while response.sendError() explicitly triggers a container-level error dispatch.
 * 
 *   When annotate a method with @ResponseStatus(code = HttpStatus.NOT_FOUND) and return void, Spring Boot 
 *   assumes you want an empty successful response that just happens to have a 404 status header.
 * 
 *   When call response.sendError(404), we are bypassing Spring and talking directly to the underlying servlet container.
 */
@ControllerAdvice 
public class CommonControllerAdvice {

    private static final Logger log = LoggerFactory.getLogger(CommonControllerAdvice.class);

    @ExceptionHandler
    @ResponseStatus(code = HttpStatus.NOT_FOUND)
    public void handleNotFound(HttpServletResponse response, NoResourceFoundException ex) throws IOException {
        log.error("Resource not found", ex);
        response.sendError(HttpStatus.NOT_FOUND.value());
    }

    @ExceptionHandler
    @ResponseStatus(code = HttpStatus.INTERNAL_SERVER_ERROR)
    public void handleOtherErrors(HttpServletResponse response, Exception ex) throws IOException {
        log.error("Unexpected error", ex);
        response.sendError(HttpStatus.INTERNAL_SERVER_ERROR.value());
    }
}
