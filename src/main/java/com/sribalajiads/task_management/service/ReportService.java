package com.sribalajiads.task_management.service;

import com.sribalajiads.task_management.dto.TaskStatsDTO;
import com.sribalajiads.task_management.entity.TaskStatus;
import com.sribalajiads.task_management.entity.User;
import com.sribalajiads.task_management.repository.TaskRepository;
import com.sribalajiads.task_management.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sribalajiads.task_management.entity.Department;
import com.sribalajiads.task_management.repository.DepartmentRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
public class ReportService {

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    // Get stats for a specific User ID (For Admin/Head view)
    public TaskStatsDTO getStatsByUserId(Long userId, LocalDate startDate, LocalDate endDate) {
        // 1. Verify User exists
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 2. Reuse the existing logic
        // We pass the user's email to the existing method, or refactor the logic.
        // Let's just refactor the logic to accept User object to avoid redundant DB calls.
        return generateStatsForUser(user, startDate, endDate);
    }

    // Refactored helper to avoid code duplication
    private TaskStatsDTO generateStatsForUser(User user, LocalDate startDate, LocalDate endDate) {
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);

        List<Object[]> results = taskRepository.getTaskCountByStatusForAssignee(
                user.getId(), startDateTime, endDateTime);

        TaskStatsDTO stats = new TaskStatsDTO();
        long totalTasks = 0;

        for (Object[] row : results) {
            TaskStatus status = (TaskStatus) row[0];
            Long count = (Long) row[1];
            totalTasks += count;

            switch (status) {
                case PENDING -> stats.setPending(count);
                case IN_PROGRESS -> stats.setInProgress(count);
                case SUBMITTED -> stats.setSubmitted(count);
                case COMPLETED -> stats.setCompleted(count);
                case REJECTED -> stats.setRejected(count);
            }
        }
        stats.setTotal(totalTasks);
        return stats;
    }

    // getEmployeeStats to use the helper
    public TaskStatsDTO getEmployeeStats(String email, LocalDate startDate, LocalDate endDate) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return generateStatsForUser(user, startDate, endDate);
    }

    // Get Stats for a specific Department
    public TaskStatsDTO getDepartmentStats(Long deptId, LocalDate startDate, LocalDate endDate) {
        // 1. Verify Department exists
        if (!departmentRepository.existsById(deptId)) {
            throw new RuntimeException("Department not found");
        }

        // 2. Prepare Date Boundaries
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);

        // 3. Run Query
        List<Object[]> results = taskRepository.getTaskCountByStatusForDepartment(
                deptId, startDateTime, endDateTime);

        // 4. Map Results to DTO (Reusing similar logic from Employee stats)
        TaskStatsDTO stats = new TaskStatsDTO();
        long totalTasks = 0;

        for (Object[] row : results) {
            TaskStatus status = (TaskStatus) row[0];
            Long count = (Long) row[1];
            totalTasks += count;

            switch (status) {
                case PENDING -> stats.setPending(count);
                case IN_PROGRESS -> stats.setInProgress(count);
                case SUBMITTED -> stats.setSubmitted(count);
                case COMPLETED -> stats.setCompleted(count);
                case REJECTED -> stats.setRejected(count);
            }
        }
        stats.setTotal(totalTasks);

        return stats;
    }
}