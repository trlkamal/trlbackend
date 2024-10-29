package com.app.trlapp.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.app.trlapp.service.DatabaseHealthService;

import java.util.Map;

@RestController
@RequestMapping("/api/health")
public class DatabaseHealthController {

    @Autowired
    private DatabaseHealthService databaseHealthService;

    @GetMapping("/db")
    public ResponseEntity<Map<String, Object>> checkDatabaseHealth() {
        Map<String, Object> result = databaseHealthService.checkDatabaseConnection();
        HttpStatus status = (HttpStatus) result.get("status");

        return new ResponseEntity<>(result, status);
    }
}
