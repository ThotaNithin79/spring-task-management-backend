package com.sribalajiads.task_management.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Autowired;
import java.sql.Connection;

@RestController
@RequestMapping("/api/v1")
public class HealthController {

    @Autowired
    private DataSource dataSource;

    @GetMapping("/health")
    public String healthCheck() {
        try (Connection conn = dataSource.getConnection()) {
            if (conn.isValid(1000)) {
                return "Application is Running and DB is Connected!";
            }
        } catch (Exception e) {
            return "Database Connection Failed: " + e.getMessage();
        }
        return "Unknown Error";
    }
}