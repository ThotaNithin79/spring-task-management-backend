package com.sribalajiads.task_management.controller;

import com.sribalajiads.task_management.dto.CreateUserRequest;
import com.sribalajiads.task_management.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import com.sribalajiads.task_management.dto.TaskResponseDTO;
import com.sribalajiads.task_management.service.TaskService;
import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;
import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private TaskService taskService;

    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'DEPT_HEAD')")
    public ResponseEntity<?> createUser(@RequestBody CreateUserRequest request) {
        try {
            // Get currently logged-in user's email from the Token
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String requesterEmail = auth.getName();

            userService.createUser(request, requesterEmail);

            return ResponseEntity.status(201).body("User created successfully");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // DELETE Endpoint - Only Super Admin can delete users
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        try {
            userService.deleteUser(id);
            return ResponseEntity.ok("User deleted successfully");
        } catch (IllegalStateException e) {
            // Return 409 Conflict if they are a Department Head
            return ResponseEntity.status(409).body(e.getMessage());
        } catch (RuntimeException e) {
            // Return 400 Bad Request for other errors (like User not found)
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Get All Tasks assigned to a specific Employee
    // Viewable by: SUPER_ADMIN
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @GetMapping("/{id}/tasks")
    public ResponseEntity<List<TaskResponseDTO>> getTasksByEmployee(
            @PathVariable Long id,
            @RequestParam(value = "startDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(value = "endDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        try {
            List<TaskResponseDTO> tasks = taskService.getTasksByEmployeeId(id, startDate, endDate);
            return ResponseEntity.ok(tasks);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
}