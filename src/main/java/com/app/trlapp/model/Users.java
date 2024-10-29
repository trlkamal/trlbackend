package com.app.trlapp.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity(name = "users") // Table name
public class Users implements UserDetails {

    @Id
    @Column(columnDefinition = "BINARY(16)") // MySQL-specific definition
    private UUID id;

    @Column(nullable = false)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(nullable = true)
    private String role;

    @PrePersist
    public void prePersist() {
        if (this.id == null) {
            this.id = UUID.randomUUID(); // Generate UUID before persisting
        }
    }

    // Getters and Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Convert role to authorities if needed, for example:
        Set<GrantedAuthority> authorities = new HashSet<>();
        // Assuming roles are simple strings, you may need to create proper authorities
        authorities.add(() -> role);
        return authorities;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true; // Adjust as necessary
    }

    @Override
    public boolean isAccountNonLocked() {
        return true; // Adjust as necessary
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true; // Adjust as necessary
    }

    @Override
    public boolean isEnabled() {
        return true; // Adjust as necessary
    }
}
