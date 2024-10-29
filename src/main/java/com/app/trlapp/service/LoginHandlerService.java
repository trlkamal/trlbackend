package com.app.trlapp.service;

import com.app.trlapp.model.LoginHandler;
import com.app.trlapp.repository.LoginHandlerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
public class LoginHandlerService {

    @Autowired
    private LoginHandlerRepository loginHandlerRepository;

    // Create or update a login handler
    public LoginHandler saveLoginHandler(LoginHandler loginHandler) {
        // Check if the loginHandler already exists
        if (loginHandler.getId() != null && loginHandlerRepository.existsById(loginHandler.getId())) {
            // Update existing login handler
            return loginHandlerRepository.save(loginHandler);
        } else {
            // Create new login handler
            return loginHandlerRepository.save(loginHandler);
        }
    }

    // Get all login handlers
    public List<LoginHandler> getAllLoginHandlers() {
        return loginHandlerRepository.findAll();
    }

    // Get a login handler by ID
    public LoginHandler getLoginHandlerById(UUID id) {
        return loginHandlerRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "LoginHandler not found"));
    }

    // Update a login handler
    public LoginHandler updateLoginHandler(UUID id, LoginHandler updatedLoginHandler) {
        LoginHandler existingLoginHandler = getLoginHandlerById(id);
        existingLoginHandler.setLoginCtrl(updatedLoginHandler.isLoginCtrl());
        return loginHandlerRepository.save(existingLoginHandler);
    }

    // Delete a login handler
    public void deleteLoginHandler(UUID id) {
        LoginHandler existingLoginHandler = getLoginHandlerById(id);
        loginHandlerRepository.delete(existingLoginHandler);
    }
}
