package com.sribalajiads.task_management.repository;

import com.sribalajiads.task_management.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
    // For Employees: See tasks assigned TO them
    List<Task> findByAssigneeId(Long assigneeId);

    // For Dept Heads: See tasks created BY them
    List<Task> findByCreatorId(Long creatorId);

    // Note: Super Admin will use the standard findAll() provided by JpaRepository

    // Aggregation Query: Returns a list of arrays like [ ["PENDING", 5], ["COMPLETED", 3] ]
    @Query("SELECT t.status, COUNT(t) FROM Task t " +
            "WHERE t.assignee.id = :assigneeId " +
            "AND t.createdAt BETWEEN :startDate AND :endDate " +
            "GROUP BY t.status")
    List<Object[]> getTaskCountByStatusForAssignee(
            @Param("assigneeId") Long assigneeId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    // Find tasks where user is Creator OR Assignee
    // Used for Department Heads so they can see their own work AND their team's work
    List<Task> findByCreatorIdOrAssigneeId(Long creatorId, Long assigneeId);
}

