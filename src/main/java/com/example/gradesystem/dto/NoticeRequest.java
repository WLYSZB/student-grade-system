package com.example.gradesystem.dto;

public class NoticeRequest {
    private String title;
    private String content;
    private Long publisherId;
    private String targetRole;
    private String status;

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public Long getPublisherId() { return publisherId; }
    public void setPublisherId(Long publisherId) { this.publisherId = publisherId; }
    public String getTargetRole() { return targetRole; }
    public void setTargetRole(String targetRole) { this.targetRole = targetRole; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
