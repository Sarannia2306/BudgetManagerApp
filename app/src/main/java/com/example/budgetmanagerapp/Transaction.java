package com.example.budgetmanagerapp;

public class Transaction {
    private String date;        // The date of the transaction
    private String description; // A brief description of the transaction
    private String category;    // The category of the transaction (e.g., Salary, Expense)
    private String type;        // The type of transaction (e.g., Income or Expense)
    private double amount;      // The amount of the transaction

    // Default constructor for Firebase
    public Transaction() { }

    // Constructor with parameters
    public Transaction(String date, String description, String category, String type, double amount) {
        this.date = date;
        this.description = description;
        this.category = category;
        this.type = type;
        this.amount = amount;
    }

    // Getters
    public String getDate() {
        return date;
    }

    public String getDescription() {
        return description;
    }

    public String getCategory() {
        return category;
    }

    public String getType() {
        return type;
    }

    public double getAmount() {
        return amount;
    }

    // Setters (optional, if you need to modify the data)
    public void setDate(String date) {
        this.date = date;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }
}
