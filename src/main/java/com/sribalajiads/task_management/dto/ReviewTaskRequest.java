package com.sribalajiads.task_management.dto;

public class ReviewTaskRequest {
    private String action; // Expected values: "ACCEPT" or "REJECT"
    private String comment; // Reason for rejection

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }
}