package com.sribalajiads.task_management.dto;

public class DepartmentDTO {
    private String name;
    private String description;
    private Long headUserId; // Optional: ID of the user to be head

    // Getters and Setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Long getHeadUserId() { return headUserId; }
    public void setHeadUserId(Long headUserId) { this.headUserId = headUserId; }
}