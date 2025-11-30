package com.sribalajiads.task_management.controller;

import com.sribalajiads.task_management.dto.CreateTaskRequest;
import com.sribalajiads.task_management.service.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import com.sribalajiads.task_management.entity.Task;
import java.util.List;

import com.sribalajiads.task_management.dto.TaskResponseDTO;

import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.MediaType;

import com.sribalajiads.task_management.dto.ReviewTaskRequest;

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

    @GetMapping
    public ResponseEntity<List<TaskResponseDTO>> getTasks() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();

        List<TaskResponseDTO> tasks = taskService.getTasksForUser(email);

        return ResponseEntity.ok(tasks);
    }

    // 1. Start Task
    @PatchMapping("/{id}/start")
    public ResponseEntity<?> startTask(@PathVariable Long id) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String email = auth.getName();

            taskService.startTask(id, email);
            return ResponseEntity.ok("Task started successfully. Status changed to IN_PROGRESS.");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 2. Submit Task (With File Upload)
    // 2. Submit Task (With File Upload)
    @PatchMapping(value = "/{id}/submit", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> submitTask(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String email = auth.getName();

            taskService.submitTask(id, email, file);
            return ResponseEntity.ok("Task submitted successfully. Pending review.");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 3. Review Task (Accept/Reject)
    @PatchMapping("/{id}/review")
    public ResponseEntity<?> reviewTask(
            @PathVariable Long id,
            @RequestBody ReviewTaskRequest request) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String email = auth.getName();

            taskService.reviewTask(id, email, request.getAction());

            String message = "Task " + request.getAction().toLowerCase() + "ed successfully.";
            return ResponseEntity.ok(message);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

}