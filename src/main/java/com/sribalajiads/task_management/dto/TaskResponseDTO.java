package com.sribalajiads.task_management.dto;

import com.sribalajiads.task_management.entity.TaskStatus;
import java.time.LocalDateTime;

public class TaskResponseDTO {
    private Long id;
    private String title;
    private String description;
    private TaskStatus status;
    private String proofUrl;
    private LocalDateTime createdAt;

    // Nested DTOs instead of full Entities
    private UserSummaryDTO creator;
    private UserSummaryDTO assignee;

    // Constructor
    public TaskResponseDTO() {}

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public TaskStatus getStatus() { return status; }
    public void setStatus(TaskStatus status) { this.status = status; }

    public String getProofUrl() { return proofUrl; }
    public void setProofUrl(String proofUrl) { this.proofUrl = proofUrl; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public UserSummaryDTO getCreator() { return creator; }
    public void setCreator(UserSummaryDTO creator) { this.creator = creator; }

    public UserSummaryDTO getAssignee() { return assignee; }
    public void setAssignee(UserSummaryDTO assignee) { this.assignee = assignee; }
}