package com.sribalajiads.task_management.dto;

public class SocketNotificationDTO {
    private String type; // e.g., "TASK_CREATED", "TASK_UPDATED", "TASK_DELETED"
    private TaskResponseDTO task; // The updated task data

    public SocketNotificationDTO(String type, TaskResponseDTO task) {
        this.type = type;
        this.task = task;
    }

    // Getters and Setters
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public TaskResponseDTO getTask() { return task; }
    public void setTask(TaskResponseDTO task) { this.task = task; }
}