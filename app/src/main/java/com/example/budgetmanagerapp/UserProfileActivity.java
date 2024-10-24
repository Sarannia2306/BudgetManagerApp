package com.example.budgetmanagerapp;

import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class UserProfileActivity extends AppCompatActivity {

    private TextView usernameDisplay, firstNameDisplay, lastNameDisplay, dobDisplay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_profile);

        // Initialize UI elements
        usernameDisplay = findViewById(R.id.username);
        firstNameDisplay = findViewById(R.id.firstName);
        lastNameDisplay = findViewById(R.id.lastName);
        dobDisplay = findViewById(R.id.dob);

        // Get data passed from ProfileActivity
        String username = getIntent().getStringExtra("username");
        String firstName = getIntent().getStringExtra("firstName");
        String lastName = getIntent().getStringExtra("lastName");
        String dob = getIntent().getStringExtra("dob");

        // Display the profile details
        usernameDisplay.setText(username);
        firstNameDisplay.setText(firstName);
        lastNameDisplay.setText(lastName);
        dobDisplay.setText(dob);
    }
}
