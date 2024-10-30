package com.app.trlapp.config;

import com.app.trlapp.repository.UsersRepository;
import com.app.trlapp.service.AuthenticationService;
import com.app.trlapp.service.CustomUserDetailsService;
import com.app.trlapp.service.SessionService;
import com.app.trlapp.service.UsersService;
import com.app.trlapp.util.JwtUtil;
import com.app.trlapp.util.PasswordEncoder;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;

import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;

import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.DelegatingFilterProxy;
import org.springframework.security.web.header.writers.XXssProtectionHeaderWriter;

import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;
import org.springframework.security.web.header.writers.ContentSecurityPolicyHeaderWriter;
import org.springframework.security.web.header.writers.ContentSecurityPolicyHeaderWriter;
@Configuration
@EnableWebSecurity
public class SecurityConfig {
	String frontendURL = System.getenv("frontendUrl");
	String  backendURL = System.getenv("backendUrl");
	String apiDocsPath= System.getenv("API_PATH");
    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;
    private final AccessDeniedHandler accessDeniedHandler;
    private final SessionService sessionService;
    private final EncryptionConfig encryptionConfig;
    private final UsersRepository userRepository;
    private final RateLimiterService rateLimiterService;
    private final CustomAuthenticationFailureHandler customAuthenticationFailureHandler;
    private final AuthenticationService authenticationService;

    
    @Autowired
    public SecurityConfig(JwtUtil jwtUtil, UserDetailsService userDetailsService, 
                          AccessDeniedHandler accessDeniedHandler, SessionService sessionService,
                          EncryptionConfig encryptionConfig, UsersRepository userRepository, 
                          RateLimiterService rateLimiterService,CustomAuthenticationFailureHandler customAuthenticationFailureHandler,AuthenticationService authenticationService) {
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
        this.accessDeniedHandler = accessDeniedHandler;
        this.sessionService = sessionService;
        this.encryptionConfig = encryptionConfig;
        this.userRepository = userRepository;
        this.rateLimiterService = rateLimiterService;
        this.customAuthenticationFailureHandler=customAuthenticationFailureHandler;
		this.authenticationService = authenticationService;
	
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        JwtAuthenticationFilter jwtAuthenticationFilter = new JwtAuthenticationFilter(jwtUtil, userDetailsService, 
            sessionService, encryptionConfig, userRepository, rateLimiterService,authenticationService);

        http
            .csrf(csrf -> csrf.disable())
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .authorizeHttpRequests(authorize -> authorize
            		.requestMatchers("/api/auth/login",
                            "/swagger-ui/**", "/v3/api-docs/**", 
                            "/swagger-ui.html","/custom-swagger-ui/**","/api/getServer","/custom-swagger-ui",apiDocsPath).permitAll()
                .requestMatchers(HttpMethod.POST, "/api/users/create").access((authentication, context) -> {
                    Authentication auth = authentication.get();
                    boolean hasAdminRole = auth.getAuthorities().stream()
                        .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_ADMIN"));
                    boolean hasCreateUserPermission = auth.getAuthorities().stream()
                        .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("CREATE_USER"));
                    return new AuthorizationDecision(hasAdminRole && hasCreateUserPermission);
                })
                .requestMatchers(HttpMethod.DELETE, "/api/users/delete/**").access((authentication, context) -> {
                    Authentication auth = authentication.get();
                    boolean hasAdminRole = auth.getAuthorities().stream()
                        .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_ADMIN"));
                    boolean hasDeleteUserPermission = auth.getAuthorities().stream()
                        .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("DELETE_USER"));
                    return new AuthorizationDecision(hasAdminRole && hasDeleteUserPermission);
                })
                .requestMatchers(HttpMethod.POST, "/api/roles").access((authentication, context) -> {
                    Authentication auth = authentication.get();
                    boolean hasAdminRole = auth.getAuthorities().stream()
                        .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_ADMIN"));
                    boolean hasCreateRolePermission = auth.getAuthorities().stream()
                        .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("CREATE_ROLE"));
                    return new AuthorizationDecision(hasAdminRole && hasCreateRolePermission);
                })
                .requestMatchers(HttpMethod.PUT, "/api/users/edit/**").access((authentication, context) -> {
                    Authentication auth = authentication.get();
                    boolean hasAdminRole = auth.getAuthorities().stream()
                        .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_ADMIN"));
                    boolean hasEditUserPermission = auth.getAuthorities().stream()
                        .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("EDIT_USER"));
                    return new AuthorizationDecision(hasAdminRole && hasEditUserPermission);
                })
                .requestMatchers(HttpMethod.GET, "/api/users/getAll").access((authentication, context) -> {
                    Authentication auth = authentication.get();
                    boolean hasAdminRole = auth.getAuthorities().stream()
                        .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_ADMIN"));
                    boolean hasUserRole = auth.getAuthorities().stream()
                        .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_USER"));
                    boolean hasViewUserPermission = auth.getAuthorities().stream()
                        .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("VIEW_USER"));
                    return new AuthorizationDecision((hasAdminRole || hasUserRole) && hasViewUserPermission);
                })
                .requestMatchers(HttpMethod.GET, "/api/users/**").access((authentication, context) -> {
                    Authentication auth = authentication.get();
                    boolean hasAdminRole = auth.getAuthorities().stream()
                        .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_ADMIN"));
                    boolean hasUserRole = auth.getAuthorities().stream()
                        .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_USER"));
                    boolean hasViewUserPermission = auth.getAuthorities().stream()
                        .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("VIEW_USER"));
                    return new AuthorizationDecision((hasAdminRole || hasUserRole) && hasViewUserPermission);
                })
                .requestMatchers(HttpMethod.GET, "/api/roles/username/**").access((authentication, context) -> {
                    Authentication auth = authentication.get();
                    boolean hasAdminRole = auth.getAuthorities().stream()
                        .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_ADMIN"));
                    boolean hasUserRole = auth.getAuthorities().stream()
                        .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_USER"));
                    boolean hasViewRolePermission = auth.getAuthorities().stream()
                        .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("VIEW_ROLE"));
                    return new AuthorizationDecision((hasAdminRole || hasUserRole) && hasViewRolePermission);
                })
                .requestMatchers(HttpMethod.PATCH, "/api/roles/**").access((authentication, context) -> {
                    Authentication auth = authentication.get();
                    boolean hasAdminRole = auth.getAuthorities().stream()
                        .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_ADMIN"));
                    boolean hasEditRolePermission = auth.getAuthorities().stream()
                        .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("EDIT_ROLE"));
                    return new AuthorizationDecision(hasAdminRole && hasEditRolePermission);
                })
                .requestMatchers(HttpMethod.DELETE, "/api/roles/**").access((authentication, context) -> {
                    Authentication auth = authentication.get();
                    boolean hasAdminRole = auth.getAuthorities().stream()
                        .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_ADMIN"));
                    boolean hasDeleteRolePermission = auth.getAuthorities().stream()
                        .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("DELETE_ROLE"));
                    return new AuthorizationDecision(hasAdminRole && hasDeleteRolePermission);
                })
                .requestMatchers(HttpMethod.POST, "/api/sales").access((authentication, context) -> {
                    Authentication auth = authentication.get();
                    boolean hasAdminRole = auth.getAuthorities().stream()
                        .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_ADMIN"));
                    boolean hasUserRole = auth.getAuthorities().stream()
                            .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_USER"));
                    boolean hasCreateSalePermission = auth.getAuthorities().stream()
                        .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("CREATE_SALE"));
                    return new AuthorizationDecision( (hasAdminRole || hasUserRole )&& hasCreateSalePermission);
                })
                .requestMatchers(HttpMethod.GET, "/api/sales/**").access((authentication, context) -> {
                    Authentication auth = authentication.get();
                    boolean hasAdminRole = auth.getAuthorities().stream()
                        .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_ADMIN"));
                    boolean hasUserRole = auth.getAuthorities().stream()
                        .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_USER"));
                    boolean hasViewSalePermission = auth.getAuthorities().stream()
                        .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("VIEW_SALE"));
                    return new AuthorizationDecision((hasAdminRole || hasUserRole) && hasViewSalePermission);
                })
                .requestMatchers(HttpMethod.GET, "/api/sales").access((authentication, context) -> {
                    Authentication auth = authentication.get();
                    boolean hasAdminRole = auth.getAuthorities().stream()
                        .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_ADMIN"));
                    boolean hasUserRole = auth.getAuthorities().stream()
                        .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_USER"));
                    boolean hasViewSalePermission = auth.getAuthorities().stream()
                        .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("VIEW_SALE"));
                    return new AuthorizationDecision((hasAdminRole || hasUserRole) && hasViewSalePermission);
                })
                .requestMatchers(HttpMethod.GET, "/api/sales/category/**").access((authentication, context) -> {
                    Authentication auth = authentication.get();
                    boolean hasAdminRole = auth.getAuthorities().stream()
                        .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_ADMIN"));
                    boolean hasUserRole = auth.getAuthorities().stream()
                        .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_USER"));
                    boolean hasViewSalePermission = auth.getAuthorities().stream()
                        .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("VIEW_SALE"));
                    return new AuthorizationDecision((hasAdminRole || hasUserRole )&& hasViewSalePermission);
                })
                .requestMatchers(HttpMethod.POST, "/api/items").access((authentication, context) -> {
                    Authentication auth = authentication.get();
                    boolean hasAdminRole = auth.getAuthorities().stream()
                        .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_ADMIN"));
                    boolean hasCreateItemPermission = auth.getAuthorities().stream()
                        .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("CREATE_ITEM"));
                    return new AuthorizationDecision(hasAdminRole && hasCreateItemPermission);
                })
                .requestMatchers(HttpMethod.GET, "/api/items").access((authentication, context) -> {
                    Authentication auth = authentication.get();
                    boolean hasAdminRole = auth.getAuthorities().stream()
                        .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_ADMIN"));
                    boolean hasUserRole = auth.getAuthorities().stream()
                        .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_USER"));
                    boolean hasViewItemPermission = auth.getAuthorities().stream()
                        .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("VIEW_ITEM"));
                    return new AuthorizationDecision((hasAdminRole || hasUserRole ) && hasViewItemPermission);
                })
                .requestMatchers(HttpMethod.PUT, "/api/items/**").access((authentication, context) -> {
                    Authentication auth = authentication.get();
                    boolean hasAdminRole = auth.getAuthorities().stream()
                        .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_ADMIN"));
                    boolean hasEditItemPermission = auth.getAuthorities().stream()
                        .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("EDIT_ITEM"));
                    return new AuthorizationDecision(hasAdminRole && hasEditItemPermission);
                })
                .requestMatchers(HttpMethod.DELETE, "/api/items/**").access((authentication, context) -> {
                    Authentication auth = authentication.get();
                    boolean hasAdminRole = auth.getAuthorities().stream()
                        .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_ADMIN"));
                    boolean hasDeleteItemPermission = auth.getAuthorities().stream()
                        .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("DELETE_ITEM"));
                    return new AuthorizationDecision(hasAdminRole && hasDeleteItemPermission);
                })
                
                
                
                .requestMatchers(HttpMethod.GET, "/api/items/search").access((authentication, context) -> {
                    Authentication auth = authentication.get();
                    boolean hasAdminRole = auth.getAuthorities().stream()
                        .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_ADMIN"));
                    boolean hasUserRole = auth.getAuthorities().stream()
                        .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_USER"));
                    boolean hasViewItemPermission = auth.getAuthorities().stream()
                        .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("VIEW_ITEM"));
                    return new AuthorizationDecision((hasAdminRole || hasUserRole) && hasViewItemPermission);
                })
                .requestMatchers(HttpMethod.POST, "/api/customers").access((authentication, context) -> {
                    Authentication auth = authentication.get();
                    boolean hasAdminRole = auth.getAuthorities().stream()
                        .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_ADMIN"));
                    boolean hasCreateCustomerPermission = auth.getAuthorities().stream()
                        .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("CREATE_CUSTOMER"));
                    return new AuthorizationDecision(hasAdminRole && hasCreateCustomerPermission);
                })
                .requestMatchers(HttpMethod.GET, "/api/customers").access((authentication, context) -> {
                    Authentication auth = authentication.get();
                    boolean hasAdminRole = auth.getAuthorities().stream()
                        .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_ADMIN"));
                    boolean hasUserRole = auth.getAuthorities().stream()
                        .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_USER"));
                    boolean hasViewCustomerPermission = auth.getAuthorities().stream()
                        .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("VIEW_CUSTOMER"));
                    return new AuthorizationDecision((hasAdminRole || hasUserRole ) && hasViewCustomerPermission);
                })
                .requestMatchers(HttpMethod.GET, "/api/customers/**").access((authentication, context) -> {
                    Authentication auth = authentication.get();
                    boolean hasAdminRole = auth.getAuthorities().stream()
                        .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_ADMIN"));
                    boolean hasUserRole = auth.getAuthorities().stream()
                        .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_USER"));
                    boolean hasViewCustomerPermission = auth.getAuthorities().stream()
                        .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("VIEW_CUSTOMER"));
                    return new AuthorizationDecision((hasAdminRole || hasUserRole )&& hasViewCustomerPermission);
                })
                .requestMatchers(HttpMethod.PUT, "/api/customers/**").access((authentication, context) -> {
                    Authentication auth = authentication.get();
                    boolean hasAdminRole = auth.getAuthorities().stream()
                        .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_ADMIN"));
                    boolean hasEditCustomerPermission = auth.getAuthorities().stream()
                        .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("EDIT_CUSTOMER"));
                    return new AuthorizationDecision(hasAdminRole && hasEditCustomerPermission);
                })
                .requestMatchers(HttpMethod.DELETE, "/api/customers/**").access((authentication, context) -> {
                    Authentication auth = authentication.get();
                    boolean hasAdminRole = auth.getAuthorities().stream()
                        .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_ADMIN"));
                    boolean hasDeleteCustomerPermission = auth.getAuthorities().stream()
                        .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("DELETE_CUSTOMER"));
                    return new AuthorizationDecision(hasAdminRole && hasDeleteCustomerPermission);
                })
                .requestMatchers(HttpMethod.PUT, "/api/login-handlers").access((authentication, context) -> {
                    Authentication auth = authentication.get();
                    boolean hasAdminRole = auth.getAuthorities().stream()
                        .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_ADMIN"));
                    boolean hasPrivacyPermission = auth.getAuthorities().stream()
                        .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("PRIVACY"));
                    return new AuthorizationDecision(hasAdminRole && hasPrivacyPermission);
                })
                .requestMatchers(HttpMethod.POST, "api/prv/{userID}").access((authentication, context) -> {
                    Authentication auth = authentication.get();
                    boolean hasAdminRole = auth.getAuthorities().stream()
                        .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_ADMIN"));
                    boolean hasPrivilegedUserPermission = auth.getAuthorities().stream()
                        .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("PRIVACY"));
                    System.out.println("Admin role: " + hasAdminRole + ", Create User permission: " + hasPrivilegedUserPermission);
                    return new AuthorizationDecision(hasAdminRole && hasPrivilegedUserPermission);
                    
                })

                .requestMatchers(HttpMethod.GET, "/actuator/**").access((authentication, context) -> {
                    Authentication auth = authentication.get();
                    boolean hasAdminRole = auth.getAuthorities().stream()
                        .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_ADMIN"));
                    boolean hasPrivacyPermission = auth.getAuthorities().stream()
                        .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("PRIVACY"));
                    return new AuthorizationDecision(hasAdminRole && hasPrivacyPermission);
                })

                .anyRequest().authenticated()  // All other requests must be authenticated
            )
            .formLogin(form -> form.disable())
            
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
            .exceptionHandling(exceptionHandling -> exceptionHandling.accessDeniedHandler(accessDeniedHandler).authenticationEntryPoint(customAuthenticationFailureHandler) );
       
        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList(frontendURL));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("AES-Encrypted-Username", "AES-Encrypted-Password", "*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
    

}
