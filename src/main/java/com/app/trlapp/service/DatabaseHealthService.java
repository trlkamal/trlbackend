package com.app.trlapp.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

@Service
public class DatabaseHealthService {

    @Autowired
    private DataSource dataSource;

    public Map<String, Object> checkDatabaseConnection() {
        Map<String, Object> result = new HashMap<>();
        try (Connection connection = dataSource.getConnection()) {
            if (connection.isValid(1000)) {
                result.put("status", HttpStatus.OK);
                result.put("message", "Database is running");
            } else {
                result.put("status", HttpStatus.SERVICE_UNAVAILABLE);
                result.put("message", "Database connection is not valid");
            }
        } catch (SQLException e) {
            result.put("status", HttpStatus.SERVICE_UNAVAILABLE);
            result.put("message", "Exception occurred while connecting to database");
            result.put("error", e.getMessage());
            result.put("sqlState", e.getSQLState());  // SQLState error code
            result.put("errorCode", e.getErrorCode()); // MySQL error code
        }

        return result;
    }
}
