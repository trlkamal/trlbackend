package com.app.trlapp.service;

import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.app.trlapp.model.Permission;
import com.app.trlapp.model.Role;
import com.app.trlapp.model.Users;
import com.app.trlapp.repository.UsersRepository;

import jakarta.transaction.Transactional;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UsersRepository usersRepository;
    private static final Logger logger = LoggerFactory.getLogger(CustomUserDetailsService.class);
    @Autowired
    private RoleService roleService;  // Assuming RoleService is already implemented

    @Override
    @Transactional
    @Cacheable(value = "usersCache", key = "#username")
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // Fetch user from the database
        Users user = usersRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));

        // Fetch role of the user
        ResponseEntity<Role> roleResponse = roleService.getRolesByUserName(user.getUsername());
        if (roleResponse.getStatusCode() != HttpStatus.OK || roleResponse.getBody() == null) {
            throw new RuntimeException("Roles not found for user: " + user.getUsername());
        }

        Role role = roleResponse.getBody();
        logger.info("User current role: " + role.getRoleName());

        // Fetch permissions from the role
        Set<String> permissions = role.getPermissions().stream()
                .map(Permission::getPermissionName)
                .collect(Collectors.toSet());

        // Convert permissions and roles to GrantedAuthority (roles prefixed with "ROLE_")
        Set<GrantedAuthority> grantedAuthorities = permissions.stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toSet());

        grantedAuthorities.add(new SimpleGrantedAuthority("ROLE_" + role.getRoleName()));

      logger.info("Granted Authorities: " + grantedAuthorities);

        // Convert Users to UserDetails
        return org.springframework.security.core.userdetails.User
                .withUsername(user.getUsername())
                .password(user.getPassword())
                .authorities(grantedAuthorities)
                .build();
    }
}
