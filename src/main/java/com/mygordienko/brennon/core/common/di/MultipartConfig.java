package com.mygordienko.brennon.core.common.di;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.multipart.support.StandardServletMultipartResolver;
import org.springframework.web.servlet.DispatcherServlet;

import jakarta.servlet.http.HttpServletRequest;

/**
 * This custom configuration is needed to have pure streaming file upload. 
 * Here we disable default Spring's multipart from data processing which does store 
 * request body in temp files (or memory). 
 * 
 * NOTE: we do it here for specific endpoint, while it can be switched off globally via properties.
 * NOTE: another important detail is to NOT use hidden form fields for file upload form on client side
 *  as it would make Spring Security to call for request body and trigger caching. On the client side we do 
 *  send CSRF token in headers for file uploading.
 */
@Configuration
public class MultipartConfig {

    @Bean(name = DispatcherServlet.MULTIPART_RESOLVER_BEAN_NAME)
    public MultipartResolver multipartResolver() {
        return new StandardServletMultipartResolver() {
            private static final Logger log = LoggerFactory.getLogger("com.mygordienko.brennon.core.common.di.StandardServletMultipartResolverCustom");
            
            @Override
            public boolean isMultipart(HttpServletRequest request) {
                if (request.getRequestURI().endsWith("/api/v1/tickets/attachments/upload")) {
                    log.info("Custom isMultipart: {}", false);
                    return false;
                }
                log.info("Custom isMultipart: {}", true);
                return super.isMultipart(request);
            }
        };
    }
}