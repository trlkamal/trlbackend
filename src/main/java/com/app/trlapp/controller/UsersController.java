package com.app.trlapp.controller;

import com.app.trlapp.config.JwtAuthenticationFilter;
import com.app.trlapp.dto.AuthRequestDto;
import com.app.trlapp.dto.ErrorResponse;
import com.app.trlapp.model.LoginHandler;
import com.app.trlapp.model.Users;
import com.app.trlapp.repository.UsersRepository;
import com.app.trlapp.service.LoginHandlerService;
import com.app.trlapp.service.SessionService;
import com.app.trlapp.service.UsersService;
import com.app.trlapp.util.AESUtil;
import com.app.trlapp.util.JwtUtil;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api") // Base URL for this controller
public class UsersController {
	@Autowired
    private  LoginHandlerService  loginHandlerService ;
	@Value("${master.key}")
    private String masterKey;
    @Value("${encrypted.secretKey}")
    private String encryptedSecretKey;
    @Value("${iv.parameter}")
    private String ivParameter;
  
    @Autowired
    private UsersService usersService;

    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private  SessionService sessionService;
    @Autowired
    private UsersRepository userRepository; 
    private static final Logger logger = LoggerFactory.getLogger(UsersController.class);
    // Create user
    @PostMapping("users/create")
    public ResponseEntity<String> createUser(@RequestBody Users user) {
        logger.info("Request to create new user: {}", user.getUsername());
        try {
            ResponseEntity<Users> response = usersService.createUser(user); // Call the service method
            
            if (response.getStatusCode() == HttpStatus.CREATED) {
                logger.info("User created successfully: {}", response.getBody().getId());
                return ResponseEntity.status(HttpStatus.CREATED).body("User created successfully"); // Return 201
            } else if (response.getStatusCode() == HttpStatus.CONFLICT) {
                logger.warn("User already exists: {}", user.getUsername());
                return ResponseEntity.status(HttpStatus.CONFLICT).body("User already exists"); // Return 409
            } else {
                logger.error("Unexpected error while creating user.");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                     .body("Error creating user: Unexpected error."); // Handle other unexpected errors
            }
        } catch (Exception e) {
            logger.error("Error creating user: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                 .body("Error creating user: " + e.getMessage()); // Return 500 for internal errors
        }}
    @GetMapping("/user/{userId}")
    public ResponseEntity<Users> getUserById(@PathVariable String userId) {
        UUID UUid = UUID.fromString(userId); // Convert string to UUID
        logger.info("Request to get user by ID: {}", UUid);
        
        ResponseEntity<Users> response = usersService.getUserById(UUid); // Call the service method
        
        if (response.getStatusCode() == HttpStatus.OK) {
            Users user = response.getBody(); // Get the user from the response
//            logger.info("User found: {}", user.getUsername());
            return ResponseEntity.ok(user); // Return 200 OK with the user
        } else {
            logger.error("User not found with ID: {}", UUid);
            return ResponseEntity.notFound().build(); // Return 404 Not Found
        }
    }
    @PostMapping("/prv")
    public ResponseEntity<?> privacyGet(@RequestBody Map<String, String> requestBody) {
        try {
            // Extract the userID from the request body
            String userID = requestBody.get("userID");

            // Decrypt the userID
            String decryptedUserId = AESUtil.decryptUserId(userID, masterKey, ivParameter);
            UUID UUid = UUID.fromString(decryptedUserId);

            // Call the service method to get user by ID
            ResponseEntity<Users> response = usersService.getUserById(UUid);

            if (response.getStatusCode() == HttpStatus.OK) {
                Users user = response.getBody(); // Get the user from the response
                String userRole = user.getRole(); // Get the role of the user
                return ResponseEntity.ok(userRole); // Return 200 OK with the user's role
            } else {
                logger.error("User not found with ID: {}", UUid);
                return ResponseEntity.notFound().build(); // Return 404 Not Found
            }

        } catch (Exception e) {
            // Log the error and return 500 Internal Server Error
            logger.error("Error retrieving user role", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }


    
    // Retrieve all users
    @GetMapping("/users/getAll")
    public ResponseEntity<?> getAllUsers() {
        logger.info("Request to fetch all users");
        try {
            ResponseEntity<List<Users>> response = usersService.getAllUsers(); // Call the service method

            if (response.getStatusCode() == HttpStatus.OK) {
                List<Users> users = response.getBody();
                logger.info("Fetched {} users", users.size());
                return ResponseEntity.ok(users); // Return 200 OK with the users
            } else if (response.getStatusCode() == HttpStatus.NO_CONTENT) {
                logger.warn("No users found.");
                return ResponseEntity.noContent().build(); // Return 204 No Content
            }
        } catch (Exception e) {
            logger.error("Error fetching users: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error fetching users"); // Handle internal server error
        }
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Unexpected error occurred"); // Handle unexpected scenarios
    }


    // Update user
    @PutMapping("/users/edit/{id}")
    public ResponseEntity<Users> updateUser(@PathVariable UUID id, @RequestBody Users updatedUser) {
        logger.info("Request to update user with ID: {}", id);
        
        ResponseEntity<Users> response = usersService.updateUser(id, updatedUser); // Call the service method

        if (response.getStatusCode() == HttpStatus.OK) {
            Users user = response.getBody(); // Extract the Users object from the ResponseEntity
            if (user != null) {
                logger.info("User updated successfully: {}", user.getUsername());
                return ResponseEntity.ok(user); // Return 200 OK with the updated user
            }
        } else if (response.getStatusCode() == HttpStatus.NOT_FOUND) {
            logger.warn("User with ID {} not found.", id);
            return ResponseEntity.notFound().build(); // Return 404 Not Found
        }

        logger.error("Unexpected error occurred while updating user with ID: {}", id);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build(); // Handle other unexpected errors
    }
    // Delete user by ID

    @DeleteMapping("users/delete/{id}")
    public ResponseEntity<String> deleteUser(@PathVariable UUID id) {
        logger.info("Request to delete user with ID: {}", id);
        try {
            boolean isDeleted = usersService.deleteUser(id); // Call service method
            if (isDeleted) {
                logger.info("User with ID {} deleted successfully.", id);
                return ResponseEntity.noContent().build(); // 204 No Content
            } else {
                logger.warn("User with ID {} not found.", id);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found"); // 404 Not Found
            }
        } catch (Exception e) {
            logger.error("Error deleting user with ID {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                 .body("Error occurred while deleting the user."); // 500 Internal Server Error
        }
    }


    @PostMapping("/auth/login")
    public ResponseEntity<?> login(@RequestHeader("aes-encrypted-username") String encryptedUsername,@RequestHeader("aes-encrypted-password") String encryptedPassword,
            HttpServletRequest request, HttpServletResponse response) throws Exception {
         //Decrypt Secrect Key 
    	 logger.info("Login attempt for user: {}", encryptedUsername);
    	 String decryptedUsername = AESUtil.decryptUserName(encryptedUsername,  masterKey, ivParameter );
    	
    	 String decryptPassword = AESUtil.decryptPassword(encryptedPassword,  masterKey, ivParameter );
    	
    	String username = decryptedUsername;
    	String password = decryptPassword;
    	//using username to get the username role 
    	// Step 4: Retrieve user role based on the username
        Optional<Users> userOptional = userRepository.findByUsername(username);
//    	//check user is "Admin" or "user"
//         
         UUID UUid = UUID.fromString("3dd6166e-30e7-45fd-a9a6-6c282cfbd7ba");
         
         LoginHandler loginHandler = loginHandlerService.getLoginHandlerById(UUid);
         
         
         if (loginHandler == null) {
             // If login handler not found, return 404 NOT FOUND
        	 logger.warn("login11 not found ..");
             return new ResponseEntity<>("Login11 handler not found", HttpStatus.NOT_FOUND);
             
         }
         
        if (userOptional.isPresent()) {
            Users user = userOptional.get();
            String role = user.getRole();  // Assuming "role" is a field in the Users entity
            // Logging for debug purposes
          
          
            // Check if the user is an admin or regular user
            if (!role.equals("admin")) {
                //just for check user role
            	 // Check if loginHandler.isLoginCtrl() is true, otherwise return an error
            	 if (!loginHandler.isLoginCtrl()) {
                     // Return a proper response to the front-end with error details
            		 logger.warn("temporarily locked the login by Admin");
                     return ResponseEntity.status(HttpStatus.FORBIDDEN)
                             .body(new ErrorResponse("temporarily locked the login by Admin", HttpStatus.FORBIDDEN.value()));
                 }
            }
       
        } 
        else
        {
        	 logger.error("user not found {} ",userOptional);
        	return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ErrorResponse("User not Found", HttpStatus.UNAUTHORIZED.value()));
    }
            // Perform authentication using the decoded username and password
            ResponseEntity<Map<String, String>> authenticationResponse = usersService.authenticate(username, password, response);

            // Initialize a response body map with the content of the authentication response
            Map<String, String> responseBody = new HashMap<>(authenticationResponse.getBody());
    

            if (authenticationResponse.getStatusCode() == HttpStatus.OK) {
                // Authentication succeeded

                // Extract tokens from the response
                String accessToken = responseBody.get("accessToken");
                String refreshToken = responseBody.get("refreshToken");

                // Generate session ID
                String sessionId = UUID.randomUUID().toString();
            
                // Store session ID in the database
                sessionService.createOrUpdateSession(username, sessionId);

                // Create cookies for access, refresh tokens, session ID, and username
                Cookie accessTokenCookie = new Cookie("accessToken", URLEncoder.encode(accessToken, StandardCharsets.UTF_8));
                accessTokenCookie.setHttpOnly(true);
                accessTokenCookie.setSecure(true); // Use true in production (HTTPS)
                accessTokenCookie.setPath("/");
                accessTokenCookie.setMaxAge(15 * 60 * 1000);  // 15 minutes

                Cookie refreshTokenCookie = new Cookie("refreshToken", URLEncoder.encode(refreshToken, StandardCharsets.UTF_8));
                refreshTokenCookie.setHttpOnly(true);
                refreshTokenCookie.setSecure(true); // Use true in production (HTTPS)
                refreshTokenCookie.setPath("/");
                refreshTokenCookie.setMaxAge(7 * 24 * 60 * 60);  // 7 days

                Cookie usernameCookie = new Cookie("username",encryptedUsername);
                usernameCookie.setHttpOnly(true);
                usernameCookie.setSecure(true); // Use true in production (HTTPS)
                usernameCookie.setPath("/");
                usernameCookie.setMaxAge(7 * 24 * 60 * 60);  // 7 days

                Cookie sessionIdCookie = new Cookie("sessionId", URLEncoder.encode(sessionId, StandardCharsets.UTF_8));
                sessionIdCookie.setHttpOnly(true);
                sessionIdCookie.setSecure(true); // Use true in production (HTTPS)
                sessionIdCookie.setPath("/");
                sessionIdCookie.setMaxAge(60 * 60);  // 60 minutes

                // Add cookies to response
                response.addCookie(accessTokenCookie);
                response.addCookie(refreshTokenCookie);
                response.addCookie(usernameCookie);
                response.addCookie(sessionIdCookie);

                // Set isAuthenticated to true
                responseBody.put("isAuthenticated", "true");
                return new ResponseEntity<>(responseBody, authenticationResponse.getStatusCode());
            } else {
                // Authentication failed

                // Set isAuthenticated to false
               
                responseBody.put("message", "badCredential");
                return new ResponseEntity<>(responseBody, authenticationResponse.getStatusCode());
            }

            // Return the updated response
          

        } 
    
    }


    
