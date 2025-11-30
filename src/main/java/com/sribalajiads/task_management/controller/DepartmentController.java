package com.sribalajiads.task_management.controller;

import com.sribalajiads.task_management.dto.DepartmentDTO;
import com.sribalajiads.task_management.entity.Department;
import com.sribalajiads.task_management.entity.Role;
import com.sribalajiads.task_management.entity.User;
import com.sribalajiads.task_management.repository.DepartmentRepository;
import com.sribalajiads.task_management.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/departments")
public class DepartmentController {

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private UserRepository userRepository;

    // 1. Create Department (SUPER_ADMIN Only)
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @PostMapping
    public ResponseEntity<?> createDepartment(@RequestBody DepartmentDTO request) {
        if (departmentRepository.existsByName(request.getName())) {
            return ResponseEntity.badRequest().body("Department with this name already exists");
        }

        Department dept = new Department();
        dept.setName(request.getName());
        dept.setDescription(request.getDescription());

        // Assign Head if provided
        if (request.getHeadUserId() != null) {
            User head = userRepository.findById(request.getHeadUserId())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // Optional: Check if this user is already a head of another dept
            if (departmentRepository.findByHeadUser(head).isPresent()) {
                return ResponseEntity.badRequest().body("User is already a Department Head");
            }

            // Logic: Promoting user to DEPT_HEAD role could happen here or in User update
            head.setRole(Role.DEPT_HEAD);
            userRepository.save(head); // Save role change

            dept.setHeadUser(head);
        }

        departmentRepository.save(dept);
        return ResponseEntity.status(201).body("Department created successfully");
    }

    // 2. Get All Departments (Available to Admin and Heads)
    @GetMapping
    public List<Department> getAllDepartments() {
        return departmentRepository.findAll();
    }

    // 3. Get Single Department
    @GetMapping("/{id}")
    public ResponseEntity<?> getDepartment(@PathVariable Long id) {
        return departmentRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // NEW ENDPOINT: Assign a Head to a Department
    // This updates BOTH the Users table and the Departments table
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @PutMapping("/{deptId}/assign-head/{userId}")
    public ResponseEntity<?> assignDepartmentHead(@PathVariable Long deptId, @PathVariable Long userId) {

        // 1. Fetch Department
        Department dept = departmentRepository.findById(deptId)
                .orElseThrow(() -> new RuntimeException("Department not found"));

        // 2. Fetch User
        User newHead = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 3. Logic: Check if this user is already head of ANOTHER department
        // (We don't want one person leading two departments)
        if (departmentRepository.findByHeadUser(newHead).isPresent()) {
            Department existingDept = departmentRepository.findByHeadUser(newHead).get();
            if (!existingDept.getId().equals(deptId)) {
                return ResponseEntity.badRequest().body("User is already the Head of " + existingDept.getName());
            }
        }

        // 4. Handle the OLD Head (if exists) - Demote them to Employee?
        // For now, let's keep it simple: we just overwrite the head.
        // The old head remains in the department but loses the 'link' in the Department table.
        // Optionally, you can change the old head's role to EMPLOYEE here.

        // 5. Update the USER
        newHead.setRole(Role.DEPT_HEAD);
        newHead.setDepartment(dept);
        userRepository.save(newHead);

        // 6. Update the DEPARTMENT (Crucial Step missing previously)
        dept.setHeadUser(newHead);
        departmentRepository.save(dept);

        return ResponseEntity.ok("Department Head updated successfully to: " + newHead.getUsername());
    }
}