package com.sribalajiads.task_management.dto;

public class TaskStatsDTO {
    private long total;
    private long pending;
    private long inProgress;
    private long submitted;
    private long completed;
    private long rejected;

    // Getters and Setters
    public long getTotal() { return total; }
    public void setTotal(long total) { this.total = total; }

    public long getPending() { return pending; }
    public void setPending(long pending) { this.pending = pending; }

    public long getInProgress() { return inProgress; }
    public void setInProgress(long inProgress) { this.inProgress = inProgress; }

    public long getSubmitted() { return submitted; }
    public void setSubmitted(long submitted) { this.submitted = submitted; }

    public long getCompleted() { return completed; }
    public void setCompleted(long completed) { this.completed = completed; }

    public long getRejected() { return rejected; }
    public void setRejected(long rejected) { this.rejected = rejected; }
}