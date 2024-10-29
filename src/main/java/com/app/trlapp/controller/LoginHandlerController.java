package com.app.trlapp.controller;

import com.app.trlapp.model.LoginHandler;
import com.app.trlapp.service.LoginHandlerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/login-handlers")
public class LoginHandlerController {

    @Autowired
    private LoginHandlerService loginHandlerService;

    private static final Logger logger = LoggerFactory.getLogger(LoginHandlerController.class);

    // Create a new login handler
    @PostMapping
    public ResponseEntity<LoginHandler> createLoginHandler(@RequestBody LoginHandler loginHandler) {
        try {
            LoginHandler createdLoginHandler = loginHandlerService.saveLoginHandler(loginHandler);
            logger.info("Created new login handler: {}", createdLoginHandler);
            return new ResponseEntity<>(createdLoginHandler, HttpStatus.CREATED);
        } catch (Exception e) {
            logger.error("Error creating login handler: {}", e.getMessage(), e);
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Get all login handlers
    @GetMapping
    public ResponseEntity<List<LoginHandler>> getAllLoginHandlers() {
        try {
            List<LoginHandler> loginHandlers = loginHandlerService.getAllLoginHandlers();
            logger.info("Retrieved {} login handlers", loginHandlers.size());
            return new ResponseEntity<>(loginHandlers, HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error retrieving login handlers: {}", e.getMessage(), e);
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Get a login handler by ID
    @GetMapping("/{id}")
    public ResponseEntity<LoginHandler> getLoginHandlerById(@PathVariable UUID id) {
        try {
            LoginHandler loginHandler = loginHandlerService.getLoginHandlerById(id);
            if (loginHandler != null) {
                logger.info("Retrieved login handler with ID: {}", id);
                return new ResponseEntity<>(loginHandler, HttpStatus.OK);
            } else {
                logger.warn("Login handler not found for ID: {}", id);
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            logger.error("Error retrieving login handler by ID {}: {}", id, e.getMessage(), e);
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Update a login handler
    @PutMapping("/{id}")
    public ResponseEntity<LoginHandler> updateLoginHandler(@PathVariable UUID id, @RequestBody LoginHandler updatedLoginHandler) {
        try {
            LoginHandler loginHandler = loginHandlerService.updateLoginHandler(id, updatedLoginHandler);
            logger.info("Updated login handler with ID: {}", id);
            return new ResponseEntity<>(loginHandler, HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error updating login handler with ID {}: {}", id, e.getMessage(), e);
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Delete a login handler
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteLoginHandler(@PathVariable UUID id) {
        try {
            loginHandlerService.deleteLoginHandler(id);
            logger.info("Deleted login handler with ID: {}", id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (Exception e) {
            logger.error("Error deleting login handler with ID {}: {}", id, e.getMessage(), e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}