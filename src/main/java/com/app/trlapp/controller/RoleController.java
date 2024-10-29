package com.app.trlapp.controller;

import com.app.trlapp.dto.RoleCreationDTO;
import com.app.trlapp.model.Role;
import com.app.trlapp.service.RoleService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

@RestController
@RequestMapping("/api/roles")
public class RoleController {

    private final RoleService roleService;
    private static final Logger logger = LoggerFactory.getLogger(RoleController.class);

    public RoleController(RoleService roleService) {
        this.roleService = roleService;
    }

    @PostMapping
    public ResponseEntity<String> createRole(@RequestBody RoleCreationDTO roleCreationDTO) {
        try {
            roleService.createRole(roleCreationDTO);
            logger.info("Role created successfully: {}", roleCreationDTO);
            return new ResponseEntity<>("Role created successfully", HttpStatus.CREATED);
        } catch (ResponseStatusException ex) {
            logger.warn("Failed to create role: {}", ex.getReason());
            return new ResponseEntity<>(ex.getReason(), ex.getStatusCode());
        } catch (Exception e) {
            logger.error("Error creating role: {}", e.getMessage(), e);
            return new ResponseEntity<>("Error creating role: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/username/{userName}")
    public ResponseEntity<Role> getRole(@PathVariable String userName) {
        ResponseEntity<Role> roleResponse = roleService.getRolesByUserName(userName);

        if (roleResponse.getStatusCode() == HttpStatus.OK && roleResponse.getBody() != null) {
            return ResponseEntity.ok(roleResponse.getBody());
        } else {
            logger.warn("Role not found for username: {}", userName);
            return ResponseEntity.notFound().build(); // 404 Not Found if the role is missing
        }
    }


    @DeleteMapping("/{roleName}")
    public ResponseEntity<Void> deleteRole(@PathVariable String roleName) {
        try {
            roleService.deleteRole(roleName);
            logger.info("Role deleted successfully: {}", roleName);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            logger.error("Error deleting role: {}", e.getMessage(), e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PatchMapping("/{userName}")
    public ResponseEntity<String> updateRolePermissions(@PathVariable String userName, @RequestBody RoleCreationDTO roleDTO) {
        try {
            roleService.updateRolePermissions(userName, roleDTO);
            logger.info("Role permissions updated successfully for user: {}", userName);
            return new ResponseEntity<>("Role permissions updated successfully", HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error updating role for user {}: {}", userName, e.getMessage(), e);
            return new ResponseEntity<>("Error updating role: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}