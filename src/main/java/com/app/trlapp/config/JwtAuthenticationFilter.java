package com.app.trlapp.config;
import com.app.trlapp.dto.ErrorResponse;
import com.app.trlapp.model.Authentication;
import com.app.trlapp.model.Users;
import com.app.trlapp.repository.UsersRepository;
import com.app.trlapp.service.AuthenticationService;
import com.app.trlapp.service.RoleService;
import com.app.trlapp.service.SessionService;
import com.app.trlapp.util.AESUtil;
import com.app.trlapp.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.filter.OncePerRequestFilter;

import org.slf4j.Logger;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtUtil jwtUtil;
    
    private final UserDetailsService userDetailsService;
    
    private final SessionService sessionService;
    
	private final EncryptionConfig encryptionConfig;
	
	 private final UsersRepository userRepository;
	 private final AuthenticationService authenticationService;
	 
	 private final RateLimiterService rateLimiterService;
	
	 private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
	 @Autowired
    public JwtAuthenticationFilter(JwtUtil jwtUtil, UserDetailsService userDetailsService , SessionService sessionService,EncryptionConfig encryptionConfig,UsersRepository userRepository,RateLimiterService rateLimiterService,AuthenticationService authenticationService) {    
		this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
        this.sessionService = sessionService;
		this.encryptionConfig = encryptionConfig ;
		this.userRepository = userRepository ;
		this.authenticationService =authenticationService;
		this.rateLimiterService = rateLimiterService;
        
        		}

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
    	 
    	 // Define the endpoints to be excluded from JWT filter
       
        
 
        String requestURI = request.getRequestURI();
        String ipAddress = getClientIpAddress(request);
       
        // Define the endpoints to be excluded from JWT filter
        String loginPath = "/api/auth/login";
        String apiDocsPath = encryptionConfig.getApiDocsPath();
        String path = request.getRequestURI();
        if (path.startsWith("/swagger-ui") || path.startsWith("/v3/api-docs")||path.startsWith(apiDocsPath)) {
            filterChain.doFilter(request, response);
            return;
        }
   
        String UserName = "ADMINvky";
        
        String usernameFromRequestUrl = null;
        
        if (requestURI.equals(loginPath)) {
        	
        	
        	 try {
            	 String SecretKey = encryptionConfig.getEncryptedSecretKey();
            	 
            	
            	 String ivParameter = encryptionConfig.getIvParameter();
            	 
            	
            	 
            	 String masterKey  = encryptionConfig.getMasterkey();
            	 
            	 
            	 usernameFromRequestUrl = AESUtil.decryptUserName(request.getHeader("aes-encrypted-username"),  masterKey, ivParameter );
            	 logger.info("Processing login request for user");
            	 
            
    		} catch (Exception e) {
    		    logger.error("Decryption failed: {}", e.getMessage());
    		    response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid username for decryption");
    		    return;
    		}
        	 Optional<Users> userOptional = userRepository.findByUsername(usernameFromRequestUrl);
        	  if (userOptional.isPresent()) {
        		  
        		  Users user = userOptional.get();
                  String role = user.getRole();   
                  if ("admin".equals(role)) {  // It's safer to use "admin".equals(role) to avoid potential NullPointerExceptions
                	    logger.info("Login username is admin role");

                	    boolean allowed = rateLimiterService.isAllowed(UserName);
                	    
                	    if (!allowed) {
                	        // Creating auth object
                	        Authentication authenticate = new   Authentication();
                	        String uuidString = user.getId().toString();
                  	        authenticate.setUserID(uuidString);
                	        authenticate.setAuthenticated(false);
                	        // Updating authentication with the auth object
                	        authenticationService.createOrUpdateAuthentication(authenticate);
                	        logger.error("User resource limit exceeds");

                	        // Setting response status and writing response
                	        response.setStatus(HttpServletResponse.SC_REQUESTED_RANGE_NOT_SATISFIABLE);
                	        response.getWriter().write("Resource limit exceeded!!");
                	        return;
                	    }
                	}

                  else {
                	  
                	  logger.info("login username is userRole role ");
        		  boolean allowed = rateLimiterService.isAllowed(ipAddress);
                  if (!allowed) {
                	  Authentication authenticate = new   Authentication();
                	  String uuidString = user.getId().toString();
          	        authenticate.setUserID(uuidString);
          	        authenticate.setAuthenticated(false);
          	        // Updating authentication with the auth object
          	        authenticationService.createOrUpdateAuthentication(authenticate);
                	  logger.error("user resource limit is exceeds");
                	  
                      response.setStatus(HttpServletResponse.SC_REQUESTED_RANGE_NOT_SATISFIABLE);
                      response.getWriter().write("Resource limit exceeded!!");
                      return;
                  }
                  
                  }
        	  }
        	  else
              {
        		  logger.error("user not found");
        		  new CustomAuthenticationFailureHandler().commence(request, response, new UsernameNotFoundException("User not found"));
              	return; // Return
              }
        	  
            // Allow the request to proceed without JWT validation        
            filterChain.doFilter(request, response);
            
            return; // Exit the method early since no JWT check is needed
        }
        
        //added the logic here retrieve session id and  username from cookies
        
     // Retrieve session ID and username from cookies
        
        String sessionIdFromCookie = getJwtFromCookies(request, "sessionId");
        
        if (sessionIdFromCookie == null) { // Check if sessionIdFromCookie is null
        	
           
            // Set response status to 401 Unauthorized
        	 handleSessionError(response, "Session expired, please log in again.");
            return; // Return early if session ID is not present
            
        }
 
        String EncryptedEUserNameFromCookies = getEncryptedEUserNameFromCookies(request, "username");
        
       
        String usernameFromCookie = null;
        
        try {
        	 String SecretKey = encryptionConfig.getEncryptedSecretKey();
        	 
        	
        	 String ivParameter = encryptionConfig.getIvParameter();
        	 
        	
        	 
        	 String masterKey  = encryptionConfig.getMasterkey();
        	 
        	
        	 
        	 
        	  usernameFromCookie = AESUtil.decryptUserName( EncryptedEUserNameFromCookies,  masterKey, ivParameter );
        	  logger.info("decryptUserName from  the cookies");
        	  
		} catch (Exception e) {
		    logger.error("Decryption failed: {}", e.getMessage());
		    response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid username for decrytion");
		    return;
		}
        Optional<Users> userOptional = userRepository.findByUsername(usernameFromCookie);
        
        if (userOptional.isPresent()) {
        	
        	Users user = userOptional.get();
            String role = user.getRole();   
            if (role.equals("admin")) {
            	 logger.info("login username is admin role ");
          	  boolean allowed = rateLimiterService.isAllowed(UserName);
                if (!allowed) {
                	 Authentication authenticate = new   Authentication();
                	 
                	 String uuidString = user.getId().toString();
           	        authenticate.setUserID(uuidString);
         	        
         	        authenticate.setAuthenticated(false);
         	        
         	        // Updating authentication with the auth object
         	        authenticationService.createOrUpdateAuthentication(authenticate);
                	logger.warn("Rate limit exceeded!!");
                    response.setStatus(HttpServletResponse.SC_REQUESTED_RANGE_NOT_SATISFIABLE);
                    response.getWriter().write("Resource limit exceeded!!");
                    return;
                }
            }
            else {
            	 logger.info("login username is user role ");
          	  
  		  boolean allowed = rateLimiterService.isAllowed(ipAddress);
            if (!allowed) {
            	 Authentication authenticate = new   Authentication();
            	 String uuidString = user.getId().toString();
       	        authenticate.setUserID(uuidString);
     	        authenticate.setAuthenticated(false);
     	        // Updating authentication with the auth object
     	        authenticationService.createOrUpdateAuthentication(authenticate);
            	logger.warn("Resource limit exceeded!!");
                response.setStatus(HttpServletResponse.SC_REQUESTED_RANGE_NOT_SATISFIABLE);
                response.getWriter().write("Resource limit exceeded!!");
                return;
            }
            
            }
  	     }
        
  	  else
  		  
        {
  		  logger.error("user is not found");
        	 response.sendError(HttpServletResponse.SC_NOT_FOUND, "User not found");	
        }

        
        if (sessionIdFromCookie != null && usernameFromCookie != null) {
        	
        	if(sessionService.isSessionValid(usernameFromCookie, sessionIdFromCookie))
        		
        	{
        //validated that with database
        
        // Check if the user is already authenticated

    	if (SecurityContextHolder.getContext().getAuthentication()== null ) {
            // Extract tokens from cookies
    		
            String accessToken = getJwtFromCookies(request, "accessToken");
            String refreshToken = getJwtFromCookies(request, "refreshToken");

            if (accessToken != null && jwtUtil.validateTokenFromCookies(accessToken)) {
                // Access token is valid
                String username = jwtUtil.getUsernameFromToken(accessToken);
                logger.info("acess token is valid");
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                
                UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());
                
                SecurityContextHolder.getContext().setAuthentication(auth);
               
            }
            
           
            else if (refreshToken != null && jwtUtil. validateRefreshTokenFromCookies(refreshToken)) {
                // Access token is invalid but refresh token is valid
            	
                String username = jwtUtil.getUsernameFromToken(refreshToken);
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                logger.error("access token is invalid but refresh token is valid");
                // Generate new access token
                String newAccessToken = jwtUtil.generateAccessToken(username);
               
                // Set new access token in cookies
                Cookie newAccessCookie = new Cookie("accessToken", URLEncoder.encode( newAccessToken, StandardCharsets.UTF_8));
                newAccessCookie.setHttpOnly(true);
                newAccessCookie.setSecure(true);  // Ensure this is true if using HTTPS
                newAccessCookie.setPath("/");
                newAccessCookie.setMaxAge(15*60*1000); // 15 minutes
                response.addCookie(newAccessCookie);
                logger.info("access token is generated again ");
                logger.info("refresh token is also generated again");
                 String newRefreshToken = jwtUtil.generateRefreshToken(username);
                
                 
                Cookie refreshTokenCookie = new Cookie("refreshToken", URLEncoder.encode(newRefreshToken, StandardCharsets.UTF_8));
                refreshTokenCookie.setHttpOnly(true);
                refreshTokenCookie.setSecure(true);
                refreshTokenCookie.setPath("/");
                refreshTokenCookie.setMaxAge(604800);
                response.addCookie(refreshTokenCookie);
                // Set authentication context with new access token
                UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());
                SecurityContextHolder.getContext().setAuthentication(auth);
                

            } else {
                // No valid tokens
            	logger.error("No validate token is provided");
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "No valid tokens provided");
                return; // Stop filter chain
            }
        }
    	
    	
    	filterChain.doFilter(request, response);
    	
        	}
        	
        	else {
        		
        		if (userOptional.isPresent()) {
        			Users user = userOptional.get();
        		 Authentication authenticate = new   Authentication();
        		 String uuidString = user.getId().toString();
       	        authenticate.setUserID(uuidString);
     	        authenticate.setAuthenticated(false);
     	        // Updating authentication with the auth object
     	        authenticationService.createOrUpdateAuthentication(authenticate);
        		}
        	    response.setStatus(440); // Custom status code for Login Timeout
        	    response.setContentType("application/json");
        	    response.setCharacterEncoding("UTF-8");
               
        	    
        	    String jsonResponse = "{\"error\": \"unauthorized\", \"message\": \"Session expired or invalid\"}";
        	    response.getWriter().write(jsonResponse); 
        	}

    	
    }
        else {
            // No session ID or username in cookies
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "No session or username found in cookies");
            return;
        }
    }
    

    private String getJwtFromCookies(HttpServletRequest request, String cookieName) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookieName.equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }
    private String getEncryptedEUserNameFromCookies(HttpServletRequest request, String cookieName) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookieName.equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }
    
    
 // Method to get client IP address
    private String getClientIpAddress(HttpServletRequest request) {
        String ipAddress = request.getHeader("X-Forwarded-For"); // Check for forwarded IP
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("X-Real-IP"); // Check for real IP
        }
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getRemoteAddr(); // Fallback to remote address
        }
        return ipAddress;
    }
    private void handleSessionError(HttpServletResponse response, String message) throws IOException {
        logger.error(message);
        response.setStatus(440); 
        response.setContentType("application/json");
        response.getWriter().write("{\"message\": \"" + message + "\"}");
    }
    
    
}
