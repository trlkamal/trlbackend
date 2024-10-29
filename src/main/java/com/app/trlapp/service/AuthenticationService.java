package com.app.trlapp.service;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.app.trlapp.dto.AuthenticateRequest;
import com.app.trlapp.dto.ErrorResponse;
import com.app.trlapp.model.Authentication;
import com.app.trlapp.model.Users;
import com.app.trlapp.repository.AuthenticationRepository;

import java.util.Optional;
import java.util.UUID;

@Service
public class AuthenticationService {
    
    private final AuthenticationRepository authenticationRepository;
    private final UsersService usersService; // Injecting UsersService to get user details
    public AuthenticationService(AuthenticationRepository authenticationRepository,UsersService usersService) {
        this.authenticationRepository = authenticationRepository;
		this.usersService = usersService;
    }

    public Authentication createOrUpdateAuthentication(Authentication authentication) {
        // Find if authentication exists for the userID
        Optional<Authentication> existingAuth = authenticationRepository.findByUserID(authentication.getUserID());

        // Fetch the user information using UsersService
     // Convert String userID to UUID
        UUID userIdUUID = UUID.fromString(authentication.getUserID());

        // Now call the getUserById method with the converted UUID
        ResponseEntity<Users> response = usersService.getUserById(userIdUUID);

     // Extract the Users object from the ResponseEntity
     Users user = response.getBody(); // getBody() retrieves the actual Users object

     // Set the user name in the authentication entity
     authentication.setUserName(user.getUsername());

        if (existingAuth.isPresent()) {
            // Update existing authentication record
            Authentication existingUser = existingAuth.get();
            existingUser.setAuthenticated(authentication.getAuthenticated());
            return authenticationRepository.save(existingUser);
        } else {
            // Create new authentication record
            return authenticationRepository.save(authentication);
        }
    }
    public ResponseEntity<AuthenticateRequest>  getAuthenticationByUserId(String userID) {
        // Fetch authentication details from the database based on the user ID
        Optional<Authentication> authentication = authenticationRepository.findByUserID(userID);
        
        if (authentication.isPresent()) {
            // Prepare response object
            Authentication auth = authentication.get();
            AuthenticateRequest authRequest = new AuthenticateRequest();
            authRequest.setUserID(auth.getUserID());
            authRequest.setAuthenticated(auth.getAuthenticated());
            
            // Return 200 OK with the authentication data
            return ResponseEntity.ok(authRequest);
        } else {
        	// Return 404 Not Found if no authentication record is found for the given user ID
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                                 .body(new AuthenticateRequest("User ID not found", false));
        }
    }


}
