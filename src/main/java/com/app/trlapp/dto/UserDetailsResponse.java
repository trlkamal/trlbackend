package com.app.trlapp.dto;

import org.springframework.security.core.userdetails.UserDetails;

public class UserDetailsResponse {
    private UserDetails userDetails;
    private String errorMessage;

    public UserDetailsResponse(UserDetails userDetails, String errorMessage) {
        this.userDetails = userDetails;
        this.errorMessage = errorMessage;
    }

    public UserDetails getUserDetails() {
        return userDetails;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}