package com.challenge.model;

public class Challenge {
    private int id;
    private String title;
    private String description;
    private double goalAmount;
    private double currentAmount;
    private int creatorId;
    private String creatorName;
    private String status;
    private String videoUrl;
    private String createdAt;
    private int supporterCount;

    public Challenge() {
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public double getGoalAmount() { return goalAmount; }
    public void setGoalAmount(double goalAmount) { this.goalAmount = goalAmount; }
    public double getCurrentAmount() { return currentAmount; }
    public void setCurrentAmount(double currentAmount) { this.currentAmount = currentAmount; }
    public int getCreatorId() { return creatorId; }
    public void setCreatorId(int creatorId) { this.creatorId = creatorId; }
    public String getCreatorName() { return creatorName; }
    public void setCreatorName(String creatorName) { this.creatorName = creatorName; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getVideoUrl() { return videoUrl; }
    public void setVideoUrl(String videoUrl) { this.videoUrl = videoUrl; }
    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
    public int getSupporterCount() { return supporterCount; }
    public void setSupporterCount(int supporterCount) { this.supporterCount = supporterCount; }

    public double getProgressPercent() {
        if (goalAmount <= 0) return 0;
        return Math.min(100, (currentAmount / goalAmount) * 100);
    }
}
