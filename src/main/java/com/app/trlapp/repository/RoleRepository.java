package com.app.trlapp.repository;

import com.app.trlapp.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {
	 Optional<Role> findByUserName(String userName);
	 Optional<Role> findById(long  id);
	void deleteByUserName(String userName);
}
