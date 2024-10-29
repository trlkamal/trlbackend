package com.app.trlapp.repository;

import com.app.trlapp.model.UserSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserSessionRepository extends JpaRepository<UserSession, Long> {
    // Find a session by username
    UserSession findByUsername(String username);

    // Optional: delete by username
    void deleteByUsername(String username);

}
