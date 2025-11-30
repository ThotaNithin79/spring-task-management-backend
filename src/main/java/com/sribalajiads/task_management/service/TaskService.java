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
}