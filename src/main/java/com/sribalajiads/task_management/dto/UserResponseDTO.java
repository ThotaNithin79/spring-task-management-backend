package com.sribalajiads.task_management.dto;

import com.sribalajiads.task_management.entity.Role;

public class UserResponseDTO {
    private Long id;
    private String username;
    private String email;
    private Role role;
    private boolean isActive;

    // Constructor
    public UserResponseDTO(Long id, String username, String email, Role role, boolean isActive) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.role = role;
        this.isActive = isActive;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }
}