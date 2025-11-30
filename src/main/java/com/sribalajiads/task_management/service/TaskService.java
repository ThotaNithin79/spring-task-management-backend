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

import java.util.List;
import java.util.Collections;

import com.sribalajiads.task_management.dto.TaskResponseDTO;
import com.sribalajiads.task_management.dto.UserSummaryDTO;
import java.util.List;
import java.util.stream.Collectors;

import java.util.Objects;

@Service
public class TaskService {

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private UserRepository userRepository;

    public Task createTask(CreateTaskRequest request, String creatorEmail) {
        // 1. Get Creator
        User creator = userRepository.findByEmail(creatorEmail)
                .orElseThrow(() -> new RuntimeException("Creator not found"));

        // 2. Get Assignee
        User assignee = userRepository.findById(request.getAssigneeId())
                .orElseThrow(() -> new RuntimeException("Assignee user not found"));

        // 3. VALIDATION LOGIC
        if (creator.getRole() == Role.DEPT_HEAD) {
            // Check if Creator has a department
            if (creator.getDepartment() == null) {
                throw new RuntimeException("You are not assigned to any department, so you cannot create tasks.");
            }

            // Check if Assignee belongs to the SAME department
            // We use Objects.equals to safely compare Long IDs (handling nulls)
            Long creatorDeptId = creator.getDepartment().getId();
            Long assigneeDeptId = (assignee.getDepartment() != null) ? assignee.getDepartment().getId() : null;

            if (!Objects.equals(creatorDeptId, assigneeDeptId)) {
                throw new RuntimeException("Access Denied: You can only assign tasks to employees within your own department.");
            }
        }
        // If SUPER_ADMIN, we skip the check (they can assign to anyone)

        // 4. Create Task
        Task task = new Task();
        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        task.setCreator(creator);
        task.setAssignee(assignee);
        task.setStatus(TaskStatus.PENDING); // Default status

        return taskRepository.save(task);
    }


    public List<TaskResponseDTO> getTasksForUser(String userEmail) {
        // 1. Get current user
        User currentUser = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<Task> tasks;

        // 2. Filter Logic (The "Secret" Logic)
        if (currentUser.getRole() == Role.SUPER_ADMIN) {
            tasks = taskRepository.findAll();
        }
        else if (currentUser.getRole() == Role.DEPT_HEAD) {
            // Only tasks created by this Head
            tasks = taskRepository.findByCreatorId(currentUser.getId());
        }
        else {
            // Only tasks assigned to this Employee
            tasks = taskRepository.findByAssigneeId(currentUser.getId());
        }

        // 3. Convert Entity List -> DTO List
        return tasks.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    // Helper method to convert Entity to DTO
    private TaskResponseDTO mapToDTO(Task task) {
        TaskResponseDTO dto = new TaskResponseDTO();
        dto.setId(task.getId());
        dto.setTitle(task.getTitle());
        dto.setDescription(task.getDescription());
        dto.setStatus(task.getStatus());
        dto.setProofUrl(task.getProofUrl());
        dto.setCreatedAt(task.getCreatedAt());

        // Map Creator
        if (task.getCreator() != null) {
            dto.setCreator(new UserSummaryDTO(
                    task.getCreator().getId(),
                    task.getCreator().getUsername(),
                    task.getCreator().getEmail()
            ));
        }

        // Map Assignee
        if (task.getAssignee() != null) {
            dto.setAssignee(new UserSummaryDTO(
                    task.getAssignee().getId(),
                    task.getAssignee().getUsername(),
                    task.getAssignee().getEmail()
            ));
        }

        return dto;
    }

}