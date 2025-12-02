package com.sribalajiads.task_management.dto;

public class DepartmentResponseDTO {
    private Long id;
    private String name;
    private String description;
    private String headName; // Useful to show who leads it

    // Constructor
    public DepartmentResponseDTO(Long id, String name, String description, String headName) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.headName = headName;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getHeadName() { return headName; }
    public void setHeadName(String headName) { this.headName = headName; }
}