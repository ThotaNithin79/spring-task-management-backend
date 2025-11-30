package com.sribalajiads.task_management.repository;

import com.sribalajiads.task_management.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
    // For Employees: See tasks assigned TO them
    List<Task> findByAssigneeId(Long assigneeId);

    // For Dept Heads: See tasks created BY them
    List<Task> findByCreatorId(Long creatorId);

    // Note: Super Admin will use the standard findAll() provided by JpaRepository
}