package com.sribalajiads.task_management.service;

import com.sribalajiads.task_management.dto.TaskStatsDTO;
import com.sribalajiads.task_management.entity.TaskStatus;
import com.sribalajiads.task_management.entity.User;
import com.sribalajiads.task_management.repository.TaskRepository;
import com.sribalajiads.task_management.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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

    public TaskStatsDTO getEmployeeStats(String email, LocalDate startDate, LocalDate endDate) {
        // 1. Get User
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 2. Convert Dates to DateTime (Start of day / End of day)
        LocalDateTime startDateTime = startDate.atStartOfDay(); // 00:00:00
        LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX); // 23:59:59.999

        // 3. Run Query
        List<Object[]> results = taskRepository.getTaskCountByStatusForAssignee(
                user.getId(), startDateTime, endDateTime);

        // 4. Map Results to DTO
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