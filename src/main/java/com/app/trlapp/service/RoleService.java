package com.app.trlapp.service;

import com.app.trlapp.dto.RoleCreationDTO;
import com.app.trlapp.model.Permission;
import com.app.trlapp.model.Role;
import com.app.trlapp.repository.PermissionRepository;
import com.app.trlapp.repository.RoleRepository;

import jakarta.transaction.Transactional;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class RoleService {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;

    private final CacheManager cacheManager;

    public RoleService(RoleRepository roleRepository, PermissionRepository permissionRepository,CacheManager cacheManager) {
        this.roleRepository = roleRepository;
        this.permissionRepository = permissionRepository;
		this.cacheManager = cacheManager;
    }
    
    @Transactional
    @CacheEvict(value = "rolesCache", allEntries = true) // Evict all roles when a new role is created
    public ResponseEntity<Void> createRole(RoleCreationDTO roleCreationDTO) {
        Optional<Role> existingRole = roleRepository.findByUserName(roleCreationDTO.getUserName());

        if (existingRole.isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build(); // 409 Conflict if username exists
        }

        Set<Permission> permissions = new HashSet<>();
        for (String permissionName : roleCreationDTO.getPermissions()) {
            Permission permission = permissionRepository.findByPermissionName(permissionName)
                    .orElseGet(() -> new Permission(permissionName));
            permissions.add(permission);
        }

        Role role = new Role();
        role.setUserName(roleCreationDTO.getUserName());
        role.setRoleName(roleCreationDTO.getRoleName());
        role.setPermissions(permissions);

        try {
            roleRepository.save(role);
            cacheManager.getCache("usersCache").clear();
            return ResponseEntity.status(HttpStatus.CREATED).build(); // 201 Created if role is saved successfully
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build(); // 500 for unexpected errors
        }
    }

    @Transactional
    @Cacheable(value = "rolesCache", key = "#userName") // Cache role by userName
    public ResponseEntity<Role> getRolesByUserName(String userName) {
        Optional<Role> role = roleRepository.findByUserName(userName);

        if (role.isPresent()) {
            return ResponseEntity.ok(role.get()); // 200 OK with the role in the body
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build(); // 404 Not Found if no role found
        }
    }

    @CacheEvict(value = "rolesCache", key = "#userName") // Evict the role cache when a role is deleted
    public ResponseEntity<Void> deleteRole(String userName) {
        Optional<Role> role = roleRepository.findByUserName(userName);

        if (role.isPresent()) {
            roleRepository.deleteByUserName(userName);
            cacheManager.getCache("usersCache").clear();
            return ResponseEntity.noContent().build(); // 204 No Content on successful deletion
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build(); // 404 Not Found if role does not exist
        }
    }

    @Transactional
    @CacheEvict(value = "rolesCache", key = "#userName") // Evict the role cache when permissions are updated
    public ResponseEntity<Void> updateRolePermissions(String userName, RoleCreationDTO roleDTO) {
        Optional<Role> roleOpt = roleRepository.findByUserName(userName);
        
        if (roleOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build(); // 404 Not Found if role not found
        }

        Role role = roleOpt.get();

        Set<Permission> updatedPermissions = new HashSet<>();
        for (String permissionName : roleDTO.getPermissions()) {
            Permission permission = permissionRepository.findByPermissionName(permissionName)
                    .orElseGet(() -> new Permission(permissionName));
            updatedPermissions.add(permission);
        }

        role.setPermissions(updatedPermissions);

        try {
            roleRepository.save(role);
            cacheManager.getCache("usersCache").clear();
            return ResponseEntity.noContent().build(); // 204 No Content on successful update
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build(); // 500 Internal Server Error
        }
    }

    @CacheEvict(value = "rolesCache", key = "#id") // Evict specific role by ID
    public ResponseEntity<Void> updateRolePermissionsById(long id, RoleCreationDTO roleDTO) {
        Optional<Role> roleOpt = roleRepository.findById(id);
        
        if (roleOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build(); // 404 Not Found if role not found by id
        }

        Role role = roleOpt.get();
        role.setUserName(roleDTO.getUserName());

        Set<Permission> updatedPermissions = new HashSet<>();
        for (String permissionName : roleDTO.getPermissions()) {
            Permission permission = permissionRepository.findByPermissionName(permissionName)
                    .orElseGet(() -> new Permission(permissionName));
            updatedPermissions.add(permission);
        }
        role.setPermissions(updatedPermissions);

        try {
            roleRepository.save(role);
            cacheManager.getCache("usersCache").clear();
            return ResponseEntity.noContent().build(); // 204 No Content on successful update
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build(); // 500 Internal Server Error in case of failure
        }
    }

}
