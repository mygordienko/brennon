package com.mygordienko.brennon.core.common.di;

import org.springframework.boot.web.error.ErrorPage;
import org.springframework.boot.web.error.ErrorPageRegistrar;
import org.springframework.boot.web.error.ErrorPageRegistry;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;

@Configuration
public class ErrorPageConfiguration implements ErrorPageRegistrar {
    @Override
    public void registerErrorPages(ErrorPageRegistry registry) {
        var notFoundPage = new ErrorPage(HttpStatus.NOT_FOUND, "/404");
        var badRequestPage = new ErrorPage(HttpStatus.BAD_REQUEST, "/400");
        registry.addErrorPages(notFoundPage, badRequestPage);
    }
}
