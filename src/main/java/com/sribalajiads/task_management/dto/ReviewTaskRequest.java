package com.sribalajiads.task_management.dto;

public class ReviewTaskRequest {
    private String action; // Expected values: "ACCEPT" or "REJECT"

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }
}