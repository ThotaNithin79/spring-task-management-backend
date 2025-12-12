package com.sribalajiads.task_management.controller;

import com.sribalajiads.task_management.dto.CreateTaskRequest;
import com.sribalajiads.task_management.service.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import com.sribalajiads.task_management.dto.PaginatedResponse;
import com.sribalajiads.task_management.entity.TaskStatus;

import com.sribalajiads.task_management.entity.Task;
import java.util.List;

import com.sribalajiads.task_management.dto.TaskResponseDTO;

import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.MediaType;

import com.sribalajiads.task_management.dto.ReviewTaskRequest;
import com.sribalajiads.task_management.dto.TaskHistoryDTO;

import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;

@RestController
@RequestMapping("/api/v1/tasks")
public class TaskController {

    @Autowired
    private TaskService taskService;

    // Only Admin and Heads can create tasks
    // @PostMapping with MULTIPART_FORM_DATA
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'DEPT_HEAD')")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> createTask(
            @RequestParam("title") String title,
            @RequestParam("description") String description,
            @RequestParam("assigneeId") Long assigneeId,
            @RequestParam(value = "file", required = false) MultipartFile file // Optional File
    ) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String creatorEmail = auth.getName();

            // Create the DTO manually from parameters
            CreateTaskRequest request = new CreateTaskRequest();
            request.setTitle(title);
            request.setDescription(description);
            request.setAssigneeId(assigneeId);

            taskService.createTask(request, creatorEmail, file);

            return ResponseEntity.status(201).body("Task created successfully with attachment.");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<List<TaskResponseDTO>> getTasks(
            @RequestParam(value = "startDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(value = "endDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();

        // Pass dates (can be null) to service
        List<TaskResponseDTO> tasks = taskService.getTasksForUser(email, startDate, endDate);

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

    // Submit Task (With File + Optional Message)
    @PostMapping(value = "/{id}/submit", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> submitTask(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "message", required = false) String message // Optional
    ) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String email = auth.getName();

            // Pass the message to service
            taskService.submitTask(id, email, file, message);

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

            // Pass the comment to service
            taskService.reviewTask(id, email, request.getAction(), request.getComment());

            String message = "Task " + request.getAction().toLowerCase() + "ed successfully.";
            return ResponseEntity.ok(message);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 4. NEW: Get Task History (Audit Trail)
    @GetMapping("/{id}/history")
    public ResponseEntity<List<TaskHistoryDTO>> getTaskHistory(@PathVariable Long id) {
        List<TaskHistoryDTO> history = taskService.getTaskHistory(id);
        return ResponseEntity.ok(history);
    }

    // EDIT TASK (Within 5 mins)
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'DEPT_HEAD')")
    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> updateTask(
            @PathVariable Long id,
            @RequestParam("title") String title,
            @RequestParam("description") String description,
            @RequestParam("assigneeId") Long assigneeId,
            @RequestParam(value = "file", required = false) MultipartFile file // Optional (Keep old file if null)
    ) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String email = auth.getName();

            taskService.updateTask(id, email, title, description, assigneeId, file);

            return ResponseEntity.ok("Task updated successfully.");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // DELETE TASK
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'DEPT_HEAD')")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteTask(@PathVariable Long id) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String email = auth.getName();

            taskService.deleteTask(id, email);

            return ResponseEntity.ok("Task deleted successfully.");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // SEARCH TASKS (Super Admin Only)
    // URL: /api/v1/tasks/search?title=Bug
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @GetMapping("/search")
    public ResponseEntity<List<TaskResponseDTO>> searchTasks(@RequestParam("title") String title) {
        try {
            List<TaskResponseDTO> tasks = taskService.searchTasksByTitle(title);
            return ResponseEntity.ok(tasks);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // GET TASKS (PAGINATED + STATUS + DATES)
    // URL Example: /api/v1/tasks/paged?page=0&size=10&status=PENDING
    @GetMapping("/paged")
    public ResponseEntity<PaginatedResponse<TaskResponseDTO>> getTasksPaged(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "startDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(value = "endDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(value = "status", required = false) TaskStatus status // <--- New Param
    ) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();

        PaginatedResponse<TaskResponseDTO> response = taskService.getTasksPaginated(email, page, size, startDate, endDate, status);

        return ResponseEntity.ok(response);
    }




}