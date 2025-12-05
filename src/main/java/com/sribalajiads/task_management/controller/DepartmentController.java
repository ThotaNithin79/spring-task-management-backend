package com.sribalajiads.task_management.controller;

import com.sribalajiads.task_management.dto.DepartmentDTO;
import com.sribalajiads.task_management.entity.Department;
import com.sribalajiads.task_management.entity.Role;
import com.sribalajiads.task_management.entity.User;
import com.sribalajiads.task_management.repository.DepartmentRepository;
import com.sribalajiads.task_management.repository.UserRepository;
import com.sribalajiads.task_management.service.DepartmentService;

import com.sribalajiads.task_management.dto.UserResponseDTO;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.sribalajiads.task_management.dto.DepartmentResponseDTO;
import java.util.stream.Collectors;

import com.sribalajiads.task_management.dto.TaskResponseDTO;
import com.sribalajiads.task_management.service.TaskService;
import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.List;

@RestController
@RequestMapping("/api/v1/departments")
public class DepartmentController {

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DepartmentService departmentService;

    @Autowired
    private TaskService taskService;

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

    // 2. Get All Departments (For Dropdowns & Lists)
    // Accessible by: SUPER_ADMIN (to select dept for new user) and DEPT_HEAD (view only)
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'DEPT_HEAD')")
    @GetMapping
    public ResponseEntity<List<DepartmentResponseDTO>> getAllDepartments() {
        List<Department> departments = departmentRepository.findAll();

        List<DepartmentResponseDTO> dtos = departments.stream()
                .map(dept -> new DepartmentResponseDTO(
                        dept.getId(),
                        dept.getName(),
                        dept.getDescription(),
                        (dept.getHeadUser() != null) ? dept.getHeadUser().getUsername() : "Unassigned"
                ))
                .collect(Collectors.toList());

        return ResponseEntity.ok(dtos);
    }

    // 3. Get Single Department
    @GetMapping("/{id}")
    public ResponseEntity<?> getDepartment(@PathVariable Long id) {
        return departmentRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Swap Department Head
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @PutMapping("/{deptId}/assign-head/{userId}")
    public ResponseEntity<?> assignDepartmentHead(@PathVariable Long deptId, @PathVariable Long userId) {

        // 1. Fetch Department
        Department dept = departmentRepository.findById(deptId)
                .orElseThrow(() -> new RuntimeException("Department not found"));

        // 2. Fetch the New Head Candidate
        User newHead = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 3. Logic: Check if New Head is already a head of ANOTHER department
        if (departmentRepository.findByHeadUser(newHead).isPresent()) {
            Department existingDept = departmentRepository.findByHeadUser(newHead).get();
            if (!existingDept.getId().equals(deptId)) {
                return ResponseEntity.badRequest().body("User is already the Head of " + existingDept.getName());
            }
        }

        // 4. === CRITICAL: Handle the OLD Head (Demotion Logic) ===
        User currentHead = dept.getHeadUser();

        // If there is an old head, and it's not the same person we are assigning
        if (currentHead != null && !currentHead.getId().equals(newHead.getId())) {
            currentHead.setRole(Role.EMPLOYEE); // Demote to Employee
            userRepository.save(currentHead);
        }

        // 5. Update the NEW Head (Promotion Logic)
        newHead.setRole(Role.DEPT_HEAD);
        newHead.setDepartment(dept); // Ensure they are assigned to this dept (if not already)
        userRepository.save(newHead);

        // 6. Update the Department Table
        dept.setHeadUser(newHead);
        departmentRepository.save(dept);

        return ResponseEntity.ok("Success: " + currentHead.getUsername() + " was demoted, and " + newHead.getUsername() + " is the new Head.");
    }

    // Get all users in a specific department
    // Useful for: Admin viewing lists, or Head selecting an assignee
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'DEPT_HEAD')")
    @GetMapping("/{id}/users")
    public ResponseEntity<?> getUsersByDepartment(@PathVariable Long id) {
        // 1. Check if Department exists
        if (!departmentRepository.existsById(id)) {
            return ResponseEntity.badRequest().body("Department not found");
        }

        // 2. Fetch Users
        List<User> users = userRepository.findByDepartmentId(id);

        // 3. Convert to DTO
        List<UserResponseDTO> userDTOs = users.stream()
                .map(user -> new UserResponseDTO(
                        user.getId(),
                        user.getUsername(),
                        user.getEmail(),
                        user.getRole(),
                        user.isActive()
                ))
                .collect(Collectors.toList());

        return ResponseEntity.ok(userDTOs);
    }

    // EDIT DEPARTMENT DETAILS (Name, Desc, Head)
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<?> updateDepartment(
            @PathVariable Long id,
            @RequestBody DepartmentDTO request) {
        try {
            departmentService.updateDepartment(id, request);
            return ResponseEntity.ok("Department updated successfully.");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Get All Tasks for a Specific Department
    // Filters: Optional Start/End Date
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @GetMapping("/{id}/tasks")
    public ResponseEntity<List<TaskResponseDTO>> getDepartmentTasks(
            @PathVariable Long id,
            @RequestParam(value = "startDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(value = "endDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        // 1. Check if Dept exists
        if (!departmentRepository.existsById(id)) {
            return ResponseEntity.badRequest().build();
        }

        // 2. Fetch Tasks
        List<TaskResponseDTO> tasks = taskService.getTasksByDepartment(id, startDate, endDate);

        return ResponseEntity.ok(tasks);
    }

}