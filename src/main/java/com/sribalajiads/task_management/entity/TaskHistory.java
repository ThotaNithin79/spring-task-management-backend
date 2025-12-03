package com.sribalajiads.task_management.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "task_history")
public class TaskHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "task_id", nullable = false)
    private Task task;

    @ManyToOne
    @JoinColumn(name = "action_by_user_id", nullable = false)
    private User actionBy;

    // Enum or String. Let's use String to be flexible, or map to TaskStatus names
    @Column(name = "action_type", nullable = false)
    private String actionType;

    @Column(columnDefinition = "TEXT")
    private String comment;

    @Column(name = "file_url")
    private String fileUrl; // The file involved in this specific action (e.g., the proof)

    @CreationTimestamp
    private LocalDateTime timestamp;

    // Constructors
    public TaskHistory() {}

    public TaskHistory(Task task, User actionBy, String actionType, String comment, String fileUrl) {
        this.task = task;
        this.actionBy = actionBy;
        this.actionType = actionType;
        this.comment = comment;
        this.fileUrl = fileUrl;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Task getTask() { return task; }
    public void setTask(Task task) { this.task = task; }

    public User getActionBy() { return actionBy; }
    public void setActionBy(User actionBy) { this.actionBy = actionBy; }

    public String getActionType() { return actionType; }
    public void setActionType(String actionType) { this.actionType = actionType; }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }

    public String getFileUrl() { return fileUrl; }
    public void setFileUrl(String fileUrl) { this.fileUrl = fileUrl; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}