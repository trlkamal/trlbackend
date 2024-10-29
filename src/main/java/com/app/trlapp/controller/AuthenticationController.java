package com.app.trlapp.controller;

import com.app.trlapp.dto.AuthenticateRequest;
import com.app.trlapp.model.Authentication;
import com.app.trlapp.service.AuthenticationService;
import com.app.trlapp.util.AESUtil;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AuthenticationController {

    @Value("${master.key}")
    private String masterKey;

    @Value("${encrypted.secretKey}")
    private String encryptedSecretKey;

    @Value("${iv.parameter}")
    private String ivParameter;

    private final AuthenticationService authenticationService;

    private static final Logger logger = LoggerFactory.getLogger(AuthenticationController.class);

    public AuthenticationController(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    // PostMapping to handle authentication creation or updating
    @PostMapping("/api/authenticate")
    public ResponseEntity<AuthenticateRequest> createOrUpdateAuthentication(@RequestBody Authentication authentication) {
        try {
            // Decrypt Secret Key 
            String decryptedUserId = AESUtil.decryptUserId(authentication.getUserID(), masterKey, ivParameter);
            authentication.setUserID(decryptedUserId);

            // Call service method to handle authentication creation or update
            Authentication auth = authenticationService.createOrUpdateAuthentication(authentication);

            AuthenticateRequest authRequest = new AuthenticateRequest();
            authRequest.setUserID(auth.getUserID());
            authRequest.setAuthenticated(auth.getAuthenticated());

            logger.info("Authentication created/updated for user ID: {}", auth.getUserID());
            return ResponseEntity.ok(authRequest);
        } catch (Exception e) {
            logger.error("Error creating/updating authentication: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Reload authentication for a user
    @PostMapping("/api/authenticate/reload")
    public ResponseEntity<AuthenticateRequest> authenticateUser(@RequestBody AuthenticateRequest request) {
        try {
            String userID = request.getUserID(); // Get the userID from the request body
            String decryptedUserId = AESUtil.decryptUserId(userID, masterKey, ivParameter);

            // Retrieve authentication data from the service using the decrypted user ID
            ResponseEntity<AuthenticateRequest> authResponse = authenticationService.getAuthenticationByUserId(decryptedUserId);

            // Extract the body from the ResponseEntity
            AuthenticateRequest authRequest = authResponse.getBody();

            if (authRequest != null) {
                logger.info("Reloaded authentication for user ID: {}", authRequest.getUserID());
                return ResponseEntity.ok(authRequest);
            } else {
                logger.warn("No authentication found for user ID: {}", decryptedUserId);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                                     .body(new AuthenticateRequest("User not found", false));
            }
        } catch (Exception e) {
            logger.error("Error reloading authentication: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}