package com.example.budgetmanagerapp;

public class UserProfile {
    private String uid;
    private String firstName;
    private String lastName;
    private String dob;
    private String email;
    private String imageUrl;
    private double balance; // Added balance field

    // Default constructor required for Firebase
    public UserProfile() {
        this.balance = 0.00; // Initialize balance to 0
    }

    // Constructor with all fields
    public UserProfile(String uid, String firstName, String lastName, String dob, String email, String imageUrl, double balance) {
        this.uid = uid;
        this.firstName = firstName;
        this.lastName = lastName;
        this.dob = dob;
        this.email = email;
        this.imageUrl = imageUrl;
        this.balance = balance;
    }

    // Getters and Setters for each field
    public String getUid() { return uid; }
    public void setUid(String uid) { this.uid = uid; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getDob() { return dob; }
    public void setDob(String dob) { this.dob = dob; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public double getBalance() { return balance; }
    public void setBalance(double balance) { this.balance = balance; }

    @Override
    public String toString() {
        return "UserProfile{" +
                "uid='" + uid + '\'' +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", dob='" + dob + '\'' +
                ", email='" + email + '\'' +
                ", imageUrl='" + imageUrl + '\'' +
                ", balance=" + balance +
                '}';
    }
}
