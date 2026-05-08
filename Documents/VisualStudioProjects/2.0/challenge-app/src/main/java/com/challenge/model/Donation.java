package com.challenge.model;

public class Donation {
    private int id;
    private int challengeId;
    private String donorName;
    private double amount;
    private String createdAt;

    public Donation() {
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getChallengeId() { return challengeId; }
    public void setChallengeId(int challengeId) { this.challengeId = challengeId; }
    public String getDonorName() { return donorName; }
    public void setDonorName(String donorName) { this.donorName = donorName; }
    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }
    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}
