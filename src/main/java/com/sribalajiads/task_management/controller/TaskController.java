package com.sribalajiads.task_management.controller;

import com.sribalajiads.task_management.dto.CreateTaskRequest;
import com.sribalajiads.task_management.service.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/tasks")
public class TaskController {

    @Autowired
    private TaskService taskService;

    // Only Admin and Heads can create tasks
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'DEPT_HEAD')")
    @PostMapping
    public ResponseEntity<?> createTask(@RequestBody CreateTaskRequest request) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String creatorEmail = auth.getName();

            taskService.createTask(request, creatorEmail);

            return ResponseEntity.status(201).body("Task created successfully");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}