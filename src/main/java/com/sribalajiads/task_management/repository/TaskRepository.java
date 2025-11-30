package com.sribalajiads.task_management.repository;

import com.sribalajiads.task_management.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
    // We will add custom query methods here in the next task (Task 10)
}