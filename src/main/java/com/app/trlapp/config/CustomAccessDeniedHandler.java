package com.app.trlapp.config;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@Component
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException exc) throws IOException {
        // Set response status code for Forbidden (403)
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        
        // Set the content type, assuming you want a plain text response
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        // Customize the response message
        String jsonResponse = "{\"error\": \"Access Denied\", \"message\": \"You do not have enough permissions to perform this action.\"}";

        // Write the response to the output stream
        response.getWriter().write(jsonResponse);
    }
}
