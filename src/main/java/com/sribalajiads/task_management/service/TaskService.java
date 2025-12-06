package com.sribalajiads.task_management.service;

import com.sribalajiads.task_management.dto.CreateTaskRequest;
import com.sribalajiads.task_management.entity.Role;
import com.sribalajiads.task_management.entity.Task;
import com.sribalajiads.task_management.entity.TaskStatus;
import com.sribalajiads.task_management.entity.User;
import com.sribalajiads.task_management.repository.TaskRepository;
import com.sribalajiads.task_management.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import com.sribalajiads.task_management.entity.TaskHistory;
import com.sribalajiads.task_management.repository.TaskHistoryRepository;
import com.sribalajiads.task_management.dto.TaskHistoryDTO;
import java.util.stream.Collectors;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Collections;

import com.sribalajiads.task_management.dto.TaskResponseDTO;
import com.sribalajiads.task_management.dto.UserSummaryDTO;
import java.util.List;
import java.util.stream.Collectors;

import java.util.Objects;

import com.sribalajiads.task_management.service.FileStorageService;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Service
public class TaskService {

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FileStorageService fileStorageService;

    @Autowired
    private TaskHistoryRepository taskHistoryRepository;


    // Now accepts MultipartFile
    public Task createTask(CreateTaskRequest request, String creatorEmail, MultipartFile file) {
        // 1. Get Creator
        User creator = userRepository.findByEmail(creatorEmail)
                .orElseThrow(() -> new RuntimeException("Creator not found"));

        // 2. Get Assignee
        User assignee = userRepository.findById(request.getAssigneeId())
                .orElseThrow(() -> new RuntimeException("Assignee user not found"));

        // 3. Validation Logic (Dept Head checks)
        if (creator.getRole() == Role.DEPT_HEAD) {
            if (creator.getDepartment() == null) {
                throw new RuntimeException("You are not assigned to any department.");
            }
            Long creatorDeptId = creator.getDepartment().getId();
            Long assigneeDeptId = (assignee.getDepartment() != null) ? assignee.getDepartment().getId() : null;

            if (!java.util.Objects.equals(creatorDeptId, assigneeDeptId)) {
                throw new RuntimeException("Access Denied: You can only assign tasks to employees within your own department.");
            }
        }

        // 4. Create Task Object
        Task task = new Task();
        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        task.setCreator(creator);
        task.setAssignee(assignee);
        task.setStatus(TaskStatus.PENDING);

        // 5. Handle Attachment File
        if (file != null && !file.isEmpty()) {
            String fileName = fileStorageService.storeFile(file);
            task.setAttachmentUrl(fileName); // Save Creator's file path
        }

        Task savedTask = taskRepository.save(task);

        // LOG HISTORY: CREATED
        taskHistoryRepository.save(new TaskHistory(
                savedTask, savedTask.getCreator(), "CREATED", "Task assigned to " + assignee.getUsername(), savedTask.getAttachmentUrl()
        ));

        return savedTask;
    }


    // Accepts optional Date Range
    public List<TaskResponseDTO> getTasksForUser(String userEmail, LocalDate startDate, LocalDate endDate) {
        User currentUser = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<Task> tasks;
        boolean isDateFilterApplied = (startDate != null && endDate != null);

        // Convert LocalDate to LocalDateTime boundaries if filters are present
        LocalDateTime startDateTime = isDateFilterApplied ? startDate.atStartOfDay() : null;
        LocalDateTime endDateTime = isDateFilterApplied ? endDate.atTime(LocalTime.MAX) : null;

        if (currentUser.getRole() == Role.SUPER_ADMIN) {
            if (isDateFilterApplied) {
                tasks = taskRepository.findAllByCreatedAtBetween(startDateTime, endDateTime);
            } else {
                tasks = taskRepository.findAll();
            }
        }
        else if (currentUser.getRole() == Role.DEPT_HEAD) {
            if (isDateFilterApplied) {
                tasks = taskRepository.findByCreatorIdOrAssigneeIdAndDateRange(
                        currentUser.getId(), startDateTime, endDateTime);
            } else {
                tasks = taskRepository.findByCreatorIdOrAssigneeId(currentUser.getId(), currentUser.getId());
            }
        }
        else {
            // Employee
            if (isDateFilterApplied) {
                tasks = taskRepository.findByAssigneeIdAndCreatedAtBetween(
                        currentUser.getId(), startDateTime, endDateTime);
            } else {
                tasks = taskRepository.findByAssigneeId(currentUser.getId());
            }
        }

        return tasks.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    // Helper method to convert Entity to DTO
    // ... Update mapToDTO helper method to include the new field ...
    private TaskResponseDTO mapToDTO(Task task) {
        TaskResponseDTO dto = new TaskResponseDTO();
        dto.setId(task.getId());
        dto.setTitle(task.getTitle());
        dto.setDescription(task.getDescription());
        dto.setStatus(task.getStatus());
        dto.setProofUrl(task.getProofUrl());       // Assignee's File
        dto.setAttachmentUrl(task.getAttachmentUrl()); // Creator's File (NEW)
        dto.setCreatedAt(task.getCreatedAt());

        // ... map creator/assignee DTOs ...
        if (task.getCreator() != null) {
            dto.setCreator(new UserSummaryDTO(
                    task.getCreator().getId(), task.getCreator().getUsername(), task.getCreator().getEmail()));
        }
        if (task.getAssignee() != null) {
            dto.setAssignee(new UserSummaryDTO(
                    task.getAssignee().getId(), task.getAssignee().getUsername(), task.getAssignee().getEmail()));
        }
        return dto;
    }

    // 1. Start Task Logic
    public void startTask(Long taskId, String userEmail) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found"));

        User currentUser = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Validate: Only Assignee can start the task
        if (!task.getAssignee().getId().equals(currentUser.getId())) {
            throw new RuntimeException("Access Denied: You are not the assignee of this task.");
        }

        // Validate: Status must be PENDING
        if (task.getStatus() != TaskStatus.PENDING) {
            throw new RuntimeException("Invalid Action: Task is not in PENDING state.");
        }

        task.setStatus(TaskStatus.IN_PROGRESS);
        taskRepository.save(task);

        // LOG HISTORY: STARTED
        taskHistoryRepository.save(new TaskHistory(
                task, currentUser, "STARTED", "Task status changed to In Progress", null
        ));
    }

    // Submit Task (Now accepts optional message)
    public void submitTask(Long taskId, String userEmail, MultipartFile file, String message) {
        // 1. Fetch Data
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found"));

        User currentUser = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 2. Validate Assignee
        if (!task.getAssignee().getId().equals(currentUser.getId())) {
            throw new RuntimeException("Access Denied: You are not the assignee of this task.");
        }

        // 3. Validate Status (Must be IN_PROGRESS or PENDING if previously rejected)
        if (task.getStatus() != TaskStatus.IN_PROGRESS && task.getStatus() != TaskStatus.PENDING) {
            throw new RuntimeException("Task must be In Progress or Pending (Rejected) to submit.");
        }

        // 4. Validate File
        if (file == null || file.isEmpty()) {
            throw new RuntimeException("Error: You must upload a proof file to submit the task.");
        }

        // 5. Store File
        String fileName = fileStorageService.storeFile(file);

        // 6. Update Task
        task.setProofUrl(fileName);
        task.setStatus(TaskStatus.SUBMITTED);
        taskRepository.save(task);

        // 7. Determine History Comment
        String historyComment = (message != null && !message.trim().isEmpty())
                ? message
                : "Task submitted for review";

        // 8. Log History (SUBMITTED)
        taskHistoryRepository.save(new TaskHistory(
                task, currentUser, "SUBMITTED", historyComment, fileName
        ));
    }

    public void reviewTask(Long taskId, String reviewerEmail, String action,  String comment) {
        // 1. Fetch Task
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found"));

        // 2. Fetch Reviewer (Logged-in User)
        User reviewer = userRepository.findByEmail(reviewerEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 3. Authorization: Only the CREATOR can review the task
        if (!task.getCreator().getId().equals(reviewer.getId())) {
            throw new RuntimeException("Access Denied: Only the task creator can review this task.");
        }

        // 4. State Check: Task should be in SUBMITTED state to be reviewed
        // (Optional: You might allow reviewing IN_PROGRESS, but SUBMITTED makes most sense)
        if (task.getStatus() != TaskStatus.SUBMITTED) {
            throw new RuntimeException("Invalid Action: Task is not in SUBMITTED state yet.");
        }

        String historyAction;
        String historyComment = (comment != null) ? comment : "No comments";

        // 5. Apply Logic
        if ("ACCEPT".equalsIgnoreCase(action)) {
            task.setStatus(TaskStatus.COMPLETED);
            historyAction = "ACCEPTED";
        }
        else if ("REJECT".equalsIgnoreCase(action)) {
            // REJECTION LOGIC:
            // Move status back to PENDING so Employee can see "Start" or "Submit" again.
            // Or move to IN_PROGRESS directly. PENDING is safer as it alerts the user.
            task.setStatus(TaskStatus.PENDING);
            historyAction = "REJECTED";
        }
        else {
            throw new RuntimeException("Invalid Action: Must be 'ACCEPT' or 'REJECT'.");
        }

        taskRepository.save(task);

        // LOG HISTORY: REVIEW (With Comment)
        taskHistoryRepository.save(new TaskHistory(
                task, reviewer, historyAction, historyComment, null
        ));
    }

    // 5. Get Task History
    public List<TaskHistoryDTO> getTaskHistory(Long taskId) {
        List<TaskHistory> historyList = taskHistoryRepository.findByTaskIdOrderByTimestampDesc(taskId);

        return historyList.stream()
                .map(h -> new TaskHistoryDTO(
                        h.getActionBy().getUsername(),
                        h.getActionType(),
                        h.getComment(),
                        h.getFileUrl(),
                        h.getTimestamp()
                ))
                .collect(Collectors.toList());
    }

    // TASK EDIT: Update Task (Allowed only within 5 minutes)
    public Task updateTask(Long taskId, String editorEmail, String title, String description, Long assigneeId, MultipartFile file) {
        // 1. Fetch Task
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found"));

        // 2. Fetch Editor (Current User)
        User editor = userRepository.findByEmail(editorEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 3. CHECK 1: Only Creator can edit
        if (!task.getCreator().getId().equals(editor.getId())) {
            throw new RuntimeException("Access Denied: Only the creator can edit this task.");
        }

        // 4. CHECK 2: Time Constraint (5 Minutes)
        LocalDateTime now = LocalDateTime.now();
        long minutesElapsed = ChronoUnit.MINUTES.between(task.getCreatedAt(), now);

        if (minutesElapsed > 5) {
            throw new RuntimeException("Edit Time Expired: You can only edit a task within 5 minutes of creation. Time elapsed: " + minutesElapsed + " mins.");
        }

        // 5. Logic: Update Assignee (Check Hierarchy Rules again)
        // Only update assignee if the ID is different
        if (!task.getAssignee().getId().equals(assigneeId)) {
            User newAssignee = userRepository.findById(assigneeId)
                    .orElseThrow(() -> new RuntimeException("New assignee not found"));

            // Re-validate Department Head Rule
            if (editor.getRole() == Role.DEPT_HEAD) {
                Long editorDeptId = editor.getDepartment().getId();
                Long assigneeDeptId = (newAssignee.getDepartment() != null) ? newAssignee.getDepartment().getId() : null;

                if (!java.util.Objects.equals(editorDeptId, assigneeDeptId)) {
                    throw new RuntimeException("Access Denied: You cannot assign tasks to employees outside your department.");
                }
            }
            task.setAssignee(newAssignee);
        }

        // 6. Update Fields
        task.setTitle(title);
        task.setDescription(description);

        // 7. Update Attachment (If a new file is uploaded)
        if (file != null && !file.isEmpty()) {
            String fileName = fileStorageService.storeFile(file);
            task.setAttachmentUrl(fileName); // Overwrite old file
        }

        return taskRepository.save(task);
    }

    // TASK DELETE (Allowed only within 5 mins AND if not started)
    public void deleteTask(Long taskId, String requesterEmail) {
        // 1. Fetch Task
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found"));

        // 2. Fetch Requester
        User currentUser = userRepository.findByEmail(requesterEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 3. CHECK 1: Only Creator can delete
        if (!task.getCreator().getId().equals(currentUser.getId())) {
            throw new RuntimeException("Access Denied: Only the creator can delete this task.");
        }

        // 4. CHECK 2: Status must be PENDING
        if (task.getStatus() != TaskStatus.PENDING) {
            throw new RuntimeException("Action Failed: Cannot delete a task that has already started or been submitted.");
        }

        // 5. CHECK 3: Time Constraint (5 Minutes)
        LocalDateTime now = LocalDateTime.now();
        long minutesElapsed = ChronoUnit.MINUTES.between(task.getCreatedAt(), now);

        if (minutesElapsed > 5) {
            throw new RuntimeException("Delete Time Expired: You can only delete a task within 5 minutes of creation.");
        }

        // 6. Optional: Delete physical attachment file if exists
        // (If you want to clean up storage, you can add file deletion logic here using Files.delete(path))

        // 7. Delete from Database
        taskRepository.delete(task);
    }

    // Get Tasks by Department (Admin View)
    public List<TaskResponseDTO> getTasksByDepartment(Long deptId, LocalDate startDate, LocalDate endDate) {

        List<Task> tasks;
        boolean isDateFilterApplied = (startDate != null && endDate != null);

        // Convert Dates
        LocalDateTime startDateTime = isDateFilterApplied ? startDate.atStartOfDay() : null;
        LocalDateTime endDateTime = isDateFilterApplied ? endDate.atTime(LocalTime.MAX) : null;

        if (isDateFilterApplied) {
            tasks = taskRepository.findByAssigneeDepartmentIdAndCreatedAtBetween(deptId, startDateTime, endDateTime);
        } else {
            tasks = taskRepository.findByAssigneeDepartmentId(deptId);
        }

        return tasks.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }


    // NEW FEATURE: Get Tasks for specific Employee (Admin View)
    public List<TaskResponseDTO> getTasksByEmployeeId(Long employeeId, LocalDate startDate, LocalDate endDate) {

        // 1. Verify User exists
        if (!userRepository.existsById(employeeId)) {
            throw new RuntimeException("User not found");
        }

        List<Task> tasks;
        boolean isDateFilterApplied = (startDate != null && endDate != null);

        // 2. Prepare Date Boundaries
        LocalDateTime startDateTime = isDateFilterApplied ? startDate.atStartOfDay() : null;
        LocalDateTime endDateTime = isDateFilterApplied ? endDate.atTime(LocalTime.MAX) : null;

        // 3. Fetch Tasks using existing Repository methods
        if (isDateFilterApplied) {
            // Reuse repository method from Task 16
            tasks = taskRepository.findByAssigneeIdAndCreatedAtBetween(employeeId, startDateTime, endDateTime);
        } else {
            // Reuse repository method from Task 10
            tasks = taskRepository.findByAssigneeId(employeeId);
        }

        // 4. Map to DTO
        return tasks.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

}