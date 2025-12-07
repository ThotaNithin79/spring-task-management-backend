package com.sribalajiads.task_management.repository;

import com.sribalajiads.task_management.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;

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

    // 1. For Super Admin (Filter All Tasks by Date)
    List<Task> findAllByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

    // 2. For Employees (Filter Assigned Tasks by Date)
    List<Task> findByAssigneeIdAndCreatedAtBetween(Long assigneeId, LocalDateTime start, LocalDateTime end);

    // 3. For Dept Heads (Filter Created OR Assigned Tasks by Date)
    @Query("SELECT t FROM Task t WHERE (t.creator.id = :userId OR t.assignee.id = :userId) AND t.createdAt BETWEEN :start AND :end")
    List<Task> findByCreatorIdOrAssigneeIdAndDateRange(
            @Param("userId") Long userId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    // 1. Fetch all tasks for a specific department (via Assignee)
    List<Task> findByAssigneeDepartmentId(Long departmentId);

    // 2. Fetch all tasks for a specific department with Date Filter
    List<Task> findByAssigneeDepartmentIdAndCreatedAtBetween(
            Long departmentId,
            LocalDateTime start,
            LocalDateTime end
    );

    // Aggregation Query for Department Stats
    @Query("SELECT t.status, COUNT(t) FROM Task t " +
            "WHERE t.assignee.department.id = :deptId " +
            "AND t.createdAt BETWEEN :startDate AND :endDate " +
            "GROUP BY t.status")
    List<Object[]> getTaskCountByStatusForDepartment(
            @Param("deptId") Long deptId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );
}

