package com.sribalajiads.task_management.service;

import com.sribalajiads.task_management.dto.CreateUserRequest;
import com.sribalajiads.task_management.entity.Department;
import com.sribalajiads.task_management.entity.Role;
import com.sribalajiads.task_management.entity.User;
import com.sribalajiads.task_management.repository.DepartmentRepository;
import com.sribalajiads.task_management.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // Added for data consistency

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // Use Transactional to ensure both User and Department tables update together
    @Transactional
    public User createUser(CreateUserRequest request, String requesterEmail) {
        // 1. Check if user already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already taken");
        }
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username already taken");
        }

        // 2. Identify the Requester (Who is trying to create this user?)
        User requester = userRepository.findByEmail(requesterEmail)
                .orElseThrow(() -> new RuntimeException("Requester not found"));

        User newUser = new User();
        newUser.setUsername(request.getUsername());
        newUser.setEmail(request.getEmail());
        newUser.setPassword(passwordEncoder.encode(request.getPassword()));
        newUser.setActive(true);

        // 3. Hierarchy Logic
        if (requester.getRole() == Role.SUPER_ADMIN) {
            // Admin can set any role and any department
            newUser.setRole(request.getRole());

            if (request.getDepartmentId() != null) {
                Department dept = departmentRepository.findById(request.getDepartmentId())
                        .orElseThrow(() -> new RuntimeException("Department not found"));
                newUser.setDepartment(dept);

                // === FIX: DATA CONSISTENCY ===
                // If Admin is creating a DEPT_HEAD, we must update the Department table
                // to link this new user as the 'head_user_id'.
                if (request.getRole() == Role.DEPT_HEAD) {
                    // 1. Save the user first to generate the ID
                    newUser = userRepository.save(newUser);

                    // 2. Update the Department
                    dept.setHeadUser(newUser);
                    departmentRepository.save(dept);

                    return newUser; // Return early because we already saved
                }
            }
        }
        else if (requester.getRole() == Role.DEPT_HEAD) {
            // Department Head Restrictions

            // Rule A: Can strictly only create EMPLOYEES
            if (request.getRole() != null && request.getRole() != Role.EMPLOYEE) {
                throw new RuntimeException("Access Denied: Department Heads can only create Employees.");
            }
            newUser.setRole(Role.EMPLOYEE);

            // Rule B: Must assign to THEIR own department
            if (requester.getDepartment() == null) {
                throw new RuntimeException("Error: You are a Department Head but not assigned to any department.");
            }
            newUser.setDepartment(requester.getDepartment());
        }
        else {
            throw new RuntimeException("Access Denied: Employees cannot create users.");
        }

        return userRepository.save(newUser);
    }

    // TASK 8: Graceful Deletion Logic
    public void deleteUser(Long userId) {
        // 1. Find the User
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 2. Check: Is this user a Department Head?
        // This relies on the 'departments' table being correct (which the fix above ensures)
        if (departmentRepository.findByHeadUser(user).isPresent()) {
            Department dept = departmentRepository.findByHeadUser(user).get();
            throw new IllegalStateException("CONFLICT: Cannot delete user '" + user.getUsername() +
                    "' because they are the Head of the '" + dept.getName() +
                    "' Department. Please assign a new Head to the department first.");
        }

        // 3. Delete the User
        userRepository.delete(user);
    }

    // Toggle Email Notifications
    public void updateNotificationPreference(String email, boolean enabled) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setEmailNotificationsEnabled(enabled);
        userRepository.save(user);
    }
}