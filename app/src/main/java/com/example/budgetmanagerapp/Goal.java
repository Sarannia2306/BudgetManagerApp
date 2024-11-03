package com.example.budgetmanagerapp;

public class Goal {
    private String goalId; // Changed to String
    private String goalName;
    private double targetAmount;
    private double savedAmount;
    private String deadline;
    private String notes;
    private String status;

    // No-argument constructor (required for Firebase)
    public Goal() {
    }

    // Parameterized constructor
    public Goal(String goalId, String goalName, double targetAmount, double savedAmount, String deadline, String notes, String status) {
        this.goalId = goalId;
        this.goalName = goalName;
        this.targetAmount = targetAmount;
        this.savedAmount = savedAmount;
        this.deadline = deadline;
        this.notes = notes;
        this.status = status;
    }

    // Getters and setters...
    public String getGoalId() {
        return goalId;
    }

    public void setGoalId(String goalId) {
        this.goalId = goalId;
    }

    public String getGoalName() {
        return goalName;
    }

    public void setGoalName(String goalName) {
        this.goalName = goalName;
    }

    public double getTargetAmount() {
        return targetAmount;
    }

    public void setTargetAmount(double targetAmount) {
        this.targetAmount = targetAmount;
    }

    public double getSavedAmount() {
        return savedAmount;
    }

    public void setSavedAmount(double savedAmount) {
        this.savedAmount = savedAmount;
    }

    public String getDeadline() {
        return deadline;
    }

    public void setDeadline(String deadline) {
        this.deadline = deadline;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    // Method to add a contribution to the saved amount
    public void addContribution(double amount) {
        if (amount > 0) {
            this.savedAmount += amount;
        }
    }

    // Check if the goal is complete
    public boolean isGoalComplete() {
        return savedAmount >= targetAmount;
    }
}
