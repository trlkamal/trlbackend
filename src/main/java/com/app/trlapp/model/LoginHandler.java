package com.app.trlapp.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.util.UUID;

@Entity
public class LoginHandler {

    @Id
    private UUID id;

    private boolean loginCtrl;

    // Constructors
    public LoginHandler() {
    }

    public LoginHandler(boolean loginCtrl) {
        this.loginCtrl = loginCtrl;
    }

    // Getters and Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public boolean isLoginCtrl() {
        return loginCtrl;
    }

    public void setLoginCtrl(boolean loginCtrl) {
        this.loginCtrl = loginCtrl;
    }
}
