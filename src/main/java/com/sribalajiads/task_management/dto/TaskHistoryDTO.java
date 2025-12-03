package com.sribalajiads.task_management.dto;

import java.time.LocalDateTime;

public class TaskHistoryDTO {
    private String actionBy;
    private String actionType;
    private String comment;
    private String fileUrl;
    private LocalDateTime timestamp;

    public TaskHistoryDTO(String actionBy, String actionType, String comment, String fileUrl, LocalDateTime timestamp) {
        this.actionBy = actionBy;
        this.actionType = actionType;
        this.comment = comment;
        this.fileUrl = fileUrl;
        this.timestamp = timestamp;
    }

    // Getters and Setters...
    public String getActionBy() { return actionBy; }
    public void setActionBy(String actionBy) { this.actionBy = actionBy; }

    public String getActionType() { return actionType; }
    public void setActionType(String actionType) { this.actionType = actionType; }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }

    public String getFileUrl() { return fileUrl; }
    public void setFileUrl(String fileUrl) { this.fileUrl = fileUrl; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}