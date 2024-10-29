package com.app.trlapp.repository;

import com.app.trlapp.model.LoginHandler;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface LoginHandlerRepository extends JpaRepository<LoginHandler, UUID> {
}

