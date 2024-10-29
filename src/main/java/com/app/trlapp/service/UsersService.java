package com.app.trlapp.service;

import org.mindrot.jbcrypt.BCrypt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.app.trlapp.model.Users;
import com.app.trlapp.repository.UsersRepository;
import com.app.trlapp.util.JwtUtil;
import com.app.trlapp.util.PasswordEncoder;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
public class UsersService {

    private static final Logger logger = LoggerFactory.getLogger(UsersService.class);

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UsersRepository usersRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public ResponseEntity<Map<String, String>> authenticate(String username, String password, HttpServletResponse response) throws Exception {
        logger.info("Attempting to authenticate user: {}", username);
        Optional<Users> userOptional = usersRepository.findByUsername(username);

        if (userOptional.isPresent()) {
            Users user = userOptional.get();
            UUID userId = user.getId();
            String userIdStr = userId.toString();

            if (BCrypt.checkpw(password, user.getPassword())) {
                logger.info("User authenticated successfully: {}", username);

                // Generate JWT tokens
                String accessToken = jwtUtil.generateAccessToken(username);
                String refreshToken = jwtUtil.generateRefreshToken(username);

            
                Map<String, String> responseMap = new HashMap<>();
                responseMap.put("message", "Authentication successful");
                responseMap.put("accessToken", accessToken);
                responseMap.put("refreshToken", refreshToken);
                responseMap.put("userid", userIdStr);

                return ResponseEntity.ok(responseMap);
            } else {
                logger.warn("Invalid password for user: {}", username);
                return new ResponseEntity<>(Map.of("error", "Invalid password"), HttpStatus.UNAUTHORIZED);
            }
        } else {
            logger.warn("User not found: {}", username);
            return new ResponseEntity<>(Map.of("error", "User not found"), HttpStatus.NOT_FOUND);
        }
    }

    public Map<String, String> refreshToken(String refreshToken) throws Exception {
        logger.info("Refreshing token");
        Map<String, String> tokens = new HashMap<>();

        if (jwtUtil.validateTokenFromCookies(refreshToken)) {
            String username = jwtUtil.getUsernameFromToken(refreshToken);

            // Generate new tokens
            String newAccessToken = jwtUtil.generateAccessToken(username);
            String newRefreshToken = jwtUtil.generateRefreshToken(username);

            tokens.put("accessToken", newAccessToken);
            tokens.put("refreshToken", newRefreshToken);

            logger.info("Tokens refreshed successfully for user: {}", username);
            return tokens;
        }

        logger.error("Invalid refresh token");
        throw new IllegalArgumentException("Invalid refresh token");
    }
   
     @Cacheable(value = "usersList")
    public ResponseEntity<List<Users>> getAllUsers() {
        try {
            logger.info("Fetching all users...");

            List<Users> users = usersRepository.findAll();

            if (users.isEmpty()) {
                logger.warn("No users found in the database.");
                return ResponseEntity.noContent().build(); // Return 204 No Content
            }

            logger.info("Successfully fetched {} users.", users.size());
            return ResponseEntity.ok(users); // Return 200 OK with the list of users

        } catch (Exception e) {
            logger.error("An error occurred while fetching users: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build(); // Return 500 Internal Server Error
        }
    }
     @CacheEvict(value = "usersList", allEntries = true)
     public ResponseEntity<Users> createUser(Users user) {
         logger.info("Attempting to create user: {}", user.getUsername());

         Optional<Users> existingUser = usersRepository.findByUsername(user.getUsername());
         if (existingUser.isPresent()) {
             logger.warn("User already exists: {}", user.getUsername());
             return ResponseEntity.status(HttpStatus.CONFLICT).build(); // Return HTTP 409
         }

         // Hash the password before saving
         user.setPassword(passwordEncoder.hashPassword(user.getPassword()));

         // Save the user
         Users savedUser = usersRepository.save(user);
         logger.info("User created successfully: {}", savedUser.getUsername());

         return new ResponseEntity<>(savedUser, HttpStatus.CREATED); // Return HTTP 201
     }

    @Cacheable(value = "usersList", key = "#id")
    public ResponseEntity<Users> getUserById(UUID id) {
        logger.info("Fetching user with ID: {}", id);

        Optional<Users> user = usersRepository.findById(id);
        if (user.isPresent()) {
            logger.info("User found with ID: {}", id);
            return ResponseEntity.ok(user.get()); // Return 200 OK with the user
        } else {
            logger.warn("User not found with ID: {}", id);
            return ResponseEntity.notFound().build(); // Return 404 Not Found
        }
    }
    @CacheEvict(value = "usersList", key = "#id")
    public ResponseEntity<Users> updateUser(UUID id, Users updatedUser) {
        logger.info("Attempting to update user with ID: {}", id);

        if (usersRepository.existsById(id)) {
            updatedUser.setId(id); // Set the ID to ensure it updates the correct user

            String rawPassword = updatedUser.getPassword();
           
            if (!isPasswordHashed(rawPassword)) {
                updatedUser.setPassword(passwordEncoder.hashPassword(rawPassword));
                logger.info("Password hashed for user with ID: {}", id);
            }

            Users savedUser = usersRepository.save(updatedUser);
            logger.info("User updated successfully: {}", savedUser.getId());

            return ResponseEntity.ok(savedUser); // Return the updated user
        } else {
            logger.warn("User not found with ID: {}", id);
            return ResponseEntity.notFound().build(); // Return 404 Not Found
        }
    }


    @CacheEvict(value = "usersList", key = "#id")
    public boolean deleteUser(UUID id) {
        logger.info("Attempting to delete user with ID: {}", id);

        if (usersRepository.existsById(id)) {
            usersRepository.deleteById(id);
            logger.info("User deleted with ID: {}", id);
            return true; // User deleted
        } else {
            logger.warn("User not found with ID: {}", id);
            return false; // User not found
        }
    }

    private boolean isPasswordHashed(String password) {
        return password.startsWith("$2a$");
    }
    public ResponseEntity<Users> getUserByUsername(String usernameFromRequestUrl) {
        Optional<Users> userOptional = usersRepository.findByUsername(usernameFromRequestUrl);

        // Check if the user is present, otherwise throw exception or return 404
        if (userOptional.isEmpty()) {
            // Option 1: Throw an exception that will be handled globally or by a controller advice
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
            
            // Option 2: Return ResponseEntity with a 404 status code
            // return new ResponseEntity<>("User not found", HttpStatus.NOT_FOUND);
        }

        // Return the user details if found
        return ResponseEntity.ok(userOptional.get());
    }
}
