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
    private String imageUrl;
    private String completionVideoUrl;
    private String thankYouMessage;
    private String createdAt;
    private int supporterCount;
    private boolean favorited;
    private boolean hasDonated;

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
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public String getCompletionVideoUrl() { return completionVideoUrl; }
    public void setCompletionVideoUrl(String completionVideoUrl) { this.completionVideoUrl = completionVideoUrl; }
    public String getThankYouMessage() { return thankYouMessage; }
    public void setThankYouMessage(String thankYouMessage) { this.thankYouMessage = thankYouMessage; }
    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
    public int getSupporterCount() { return supporterCount; }
    public void setSupporterCount(int supporterCount) { this.supporterCount = supporterCount; }
    public boolean isFavorited() { return favorited; }
    public void setFavorited(boolean favorited) { this.favorited = favorited; }
    public boolean isHasDonated() { return hasDonated; }
    public void setHasDonated(boolean hasDonated) { this.hasDonated = hasDonated; }

    public double getProgressPercent() {
        if (goalAmount <= 0) return 0;
        return Math.min(100, (currentAmount / goalAmount) * 100);
    }
}
