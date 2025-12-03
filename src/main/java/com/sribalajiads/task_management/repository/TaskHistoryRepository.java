package com.sribalajiads.task_management.repository;

import com.sribalajiads.task_management.entity.TaskHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskHistoryRepository extends JpaRepository<TaskHistory, Long> {
    // To fetch history for a specific task, ordered by latest first
    List<TaskHistory> findByTaskIdOrderByTimestampDesc(Long taskId);
}