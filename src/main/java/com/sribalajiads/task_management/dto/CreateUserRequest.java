package com.sribalajiads.task_management.dto;

import com.sribalajiads.task_management.entity.Role;

public class CreateUserRequest {
    private String username;
    private String email;
    private String password;
    private Role role;       // Optional for Dept Head (defaults to EMPLOYEE)
    private Long departmentId; // Optional for Dept Head (defaults to their own)

    // Getters and Setters
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }

    public Long getDepartmentId() { return departmentId; }
    public void setDepartmentId(Long departmentId) { this.departmentId = departmentId; }
}