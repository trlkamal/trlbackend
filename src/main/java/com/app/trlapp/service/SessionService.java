package com.app.trlapp.service;

import com.app.trlapp.model.UserSession;
import com.app.trlapp.repository.UserSessionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class SessionService {

    @Autowired
    private UserSessionRepository userSessionRepository;

    // Create or update a session
    public void createOrUpdateSession(String username, String sessionId) {
        UserSession existingSession = userSessionRepository.findByUsername(username);
        if (existingSession != null) {
            // Update existing session
            existingSession.setSessionId(sessionId);
            userSessionRepository.save(existingSession);
            } else {
            // Create new session
            UserSession newSession = new UserSession(username, sessionId);
            userSessionRepository.save(newSession);
        }
    }

    // Validate if the session is active
    public boolean isSessionValid(String username, String sessionId) {
        UserSession session = userSessionRepository.findByUsername(username);
        if (session != null) {
            return session.getSessionId().equals(sessionId);
        }
        return false;
    }

    // Delete session by username (on logout or session expiration)
    public void deleteSession(String username) {
        userSessionRepository.deleteByUsername(username);
    }
}
