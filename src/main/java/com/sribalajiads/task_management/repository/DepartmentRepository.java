package com.sribalajiads.task_management.repository;

import com.sribalajiads.task_management.entity.Department;
import com.sribalajiads.task_management.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface DepartmentRepository extends JpaRepository<Department, Long> {
    boolean existsByName(String name);
    Optional<Department> findByHeadUser(User user);
}