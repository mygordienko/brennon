package com.mygordienko.brennon.core.auth.adapter.web;

import java.io.IOException;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.authentication.Http403ForbiddenEntryPoint;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * By default Spring Security remembers a request that triggred authentication in order to
 * get the client back to requested resource after authentication is done.
 * 
 * That is not what we need with HTMX. HTMX requests made with expired session must trigger full
 * page reload, otherwise users would see just a fragment after re-login.
 * 
 * Sets `HX-Refresh` header to force HTMX to do a full page reload.
 */
public class HxRefreshHeaderAuthenticationEntryPoint implements AuthenticationEntryPoint {

    /**
     * Default entry point, just to proceed with the flow and get the response delivered to the client.
     */
    private final AuthenticationEntryPoint defaultEntryPoint;

    public HxRefreshHeaderAuthenticationEntryPoint() {
        this.defaultEntryPoint = new Http403ForbiddenEntryPoint();
    }

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {
        response.setHeader("HX-Refresh", "true");
        defaultEntryPoint.commence(request, response, authException);
    }
}
