package com.app.trlapp.dto;

public class AuthenticateRequest {
    private String userID;
    private boolean isAuthenticated;
    private String errorMessage;  // 
//    
    public AuthenticateRequest() {
        // Default constructor
    }

    public AuthenticateRequest(String errorMessage, boolean isAuthenticated) {
        this.errorMessage = errorMessage;
        this.isAuthenticated = isAuthenticated;
    }
	// Getters and Setters
    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public boolean isAuthenticated() {
        return isAuthenticated;
    }

    public void setAuthenticated(boolean authenticated) {
        isAuthenticated = authenticated;
    }
}
