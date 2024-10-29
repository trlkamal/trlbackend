package com.app.trlapp.model;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class Authentication {

    @Id
    private UUID id;
   @Column
    private String userID;
   @Column
   private String userName;
    private boolean authenticated;

    public Authentication() {
        this.id = UUID.randomUUID(); // Generate a new UUID
    }

    // Getters and Setters
    public UUID getId() {
        return id;
    }
 
    public void setId(UUID id) {
        this.id = id;
    }

    public String  getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public boolean getAuthenticated() {
        return authenticated;
    }

    public void setAuthenticated(boolean authenticated) {
        this.authenticated = authenticated;
    }
public String getUserName()
{
	return userName;
}

public void setUserName(String userName)
{
	  this.userName = userName;
}
}
