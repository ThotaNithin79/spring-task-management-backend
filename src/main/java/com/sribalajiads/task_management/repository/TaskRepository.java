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

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.sribalajiads.task_management.entity.TaskStatus;

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

    // SEARCH: Find tasks where title contains the keyword (Case Insensitive)
    List<Task> findByTitleContainingIgnoreCase(String title);

    // SEARCH WITHIN DEPARTMENT
    // Finds tasks where Assignee belongs to specific Dept AND Title matches keyword
    List<Task> findByAssigneeDepartmentIdAndTitleContainingIgnoreCase(Long departmentId, String title);

    // SEARCH WITHIN EMPLOYEE TASKS
    // Finds tasks where Assignee ID matches AND Title matches keyword
    List<Task> findByAssigneeIdAndTitleContainingIgnoreCase(Long assigneeId, String title);


    // 1. Find ALL with Pagination (Super Admin)
    Page<Task> findAll(Pageable pageable);

    // 2. Find ALL with Date Filter + Pagination (Super Admin)
    Page<Task> findAllByCreatedAtBetween(LocalDateTime start, LocalDateTime end, Pageable pageable);

    // 3. Find by Assignee + Pagination (Employee)
    Page<Task> findByAssigneeId(Long assigneeId, Pageable pageable);

    // 4. Find by Assignee + Date Filter + Pagination (Employee)
    Page<Task> findByAssigneeIdAndCreatedAtBetween(Long assigneeId, LocalDateTime start, LocalDateTime end, Pageable pageable);

    // 5. Find for Dept Head (Creator OR Assignee) + Pagination
    @Query("SELECT t FROM Task t WHERE t.creator.id = :userId OR t.assignee.id = :userId")
    Page<Task> findByCreatorIdOrAssigneeId(@Param("userId") Long userId, Pageable pageable);

    // 6. Find for Dept Head + Date + Pagination
    @Query("SELECT t FROM Task t WHERE (t.creator.id = :userId OR t.assignee.id = :userId) AND t.createdAt BETWEEN :start AND :end")
    Page<Task> findByCreatorIdOrAssigneeIdAndDateRange(
            @Param("userId") Long userId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end,
            Pageable pageable
    );

    // 1. SUPER ADMIN: Filter by Status (Optional) AND Date (Optional)
    @Query("SELECT t FROM Task t WHERE " +
            "(:status IS NULL OR t.status = :status) AND " +
            "(:start IS NULL OR t.createdAt >= :start) AND " +
            "(:end IS NULL OR t.createdAt <= :end)")
    Page<Task> findTasksForAdmin(
            @Param("status") TaskStatus status,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end,
            Pageable pageable
    );

    // 2. DEPARTMENT HEAD: Filter by (Creator OR Assignee) + Status + Date
    @Query("SELECT t FROM Task t WHERE " +
            "(t.creator.id = :userId OR t.assignee.id = :userId) AND " +
            "(:status IS NULL OR t.status = :status) AND " +
            "(:start IS NULL OR t.createdAt >= :start) AND " +
            "(:end IS NULL OR t.createdAt <= :end)")
    Page<Task> findTasksForHead(
            @Param("userId") Long userId,
            @Param("status") TaskStatus status,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end,
            Pageable pageable
    );

    // 3. EMPLOYEE: Filter by Assignee + Status + Date
    @Query("SELECT t FROM Task t WHERE " +
            "t.assignee.id = :userId AND " +
            "(:status IS NULL OR t.status = :status) AND " +
            "(:start IS NULL OR t.createdAt >= :start) AND " +
            "(:end IS NULL OR t.createdAt <= :end)")
    Page<Task> findTasksForEmployee(
            @Param("userId") Long userId,
            @Param("status") TaskStatus status,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end,
            Pageable pageable
    );
}

