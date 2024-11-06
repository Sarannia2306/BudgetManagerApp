package com.example.budgetmanagerapp;

import android.app.DatePickerDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Calendar;

public class AddGoalActivity extends AppCompatActivity {

    private static final String CHANNEL_ID = "goal_notifications";
    private EditText goalNameInput, targetAmountInput, deadlineInput, notesInput, initialSaveInput;
    private Button saveGoalButton;
    private DatabaseReference userDatabaseRef, goalsDatabaseRef;
    private FirebaseAuth mAuth;
    private double currentBalance;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_goals);

       mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {
            String uid = currentUser.getUid();
            userDatabaseRef = FirebaseDatabase.getInstance().getReference("Users").child(uid);
            goalsDatabaseRef = userDatabaseRef.child("Goals");
            fetchCurrentBalance();
        } else {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        goalNameInput = findViewById(R.id.goalNameInput);
        targetAmountInput = findViewById(R.id.targetAmountInput);
        deadlineInput = findViewById(R.id.deadlineInput);
        notesInput = findViewById(R.id.notesInput);
        initialSaveInput = findViewById(R.id.initialSaveInput);
        saveGoalButton = findViewById(R.id.saveGoalButton);
        ImageView logoImageView = findViewById(R.id.logoImageView);

        deadlineInput.setOnClickListener(v -> showDatePickerDialog());

        saveGoalButton.setOnClickListener(v -> saveGoal());

        logoImageView.setOnClickListener(v -> {
            Intent homeIntent = new Intent(AddGoalActivity.this, HomePageActivity.class);
            startActivity(homeIntent);
        });

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setOnNavigationItemSelectedListener(this::onNavigationItemSelected);

        createNotificationChannel();
    }

    private void fetchCurrentBalance() {
        userDatabaseRef.child("balance").get().addOnSuccessListener(snapshot -> {
            if (snapshot.exists()) {
                currentBalance = snapshot.getValue(Double.class);
            }
        }).addOnFailureListener(e -> Toast.makeText(this, "Failed to fetch balance", Toast.LENGTH_SHORT).show());
    }

    private void showDatePickerDialog() {
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this, (view, selectedYear, selectedMonth, selectedDay) -> {
            String selectedDate = String.format("%02d/%02d/%d", selectedDay, selectedMonth + 1, selectedYear);
            deadlineInput.setText(selectedDate);
        }, year, month, day);
        datePickerDialog.show();
    }

    private void saveGoal() {
        String goalName = goalNameInput.getText().toString().trim();
        double targetAmount;
        double initialSaveAmount;
        String deadline = deadlineInput.getText().toString().trim();
        String notes = notesInput.getText().toString().trim();

        try {
            targetAmount = Double.parseDouble(targetAmountInput.getText().toString().trim());
            initialSaveAmount = Double.parseDouble(initialSaveInput.getText().toString().trim());
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Please enter valid amounts", Toast.LENGTH_SHORT).show();
            return;
        }

        if (goalName.isEmpty() || targetAmount <= 0 || deadline.isEmpty()) {
            Toast.makeText(this, "Please fill in all required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (initialSaveAmount > currentBalance) {
            Toast.makeText(this, "Insufficient balance for initial save amount", Toast.LENGTH_SHORT).show();
            return;
        }

        // Deduct initial save amount from balance
        currentBalance -= initialSaveAmount;
        userDatabaseRef.child("balance").setValue(currentBalance);

        // Create a new goal
        String goalId = goalsDatabaseRef.push().getKey();
        if (goalId != null) {
            Goal newGoal = new Goal(goalId, goalName, targetAmount, initialSaveAmount, deadline, notes, "In Progress");
            goalsDatabaseRef.child(goalId).setValue(newGoal).addOnSuccessListener(aVoid -> {
                Toast.makeText(this, "Goal added successfully", Toast.LENGTH_SHORT).show();
                updateProgress(newGoal);
            }).addOnFailureListener(e -> Toast.makeText(this, "Failed to add goal: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        }
    }

    private void updateProgress(Goal goal) {
        if (goal.isGoalComplete()) {
            sendGoalCompletionNotification(goal.getGoalName());
        }
    }

    private void sendGoalCompletionNotification(String goalName) {
        Log.d("NotificationDebug", "Sending notification for goal: " + goalName);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.goal_completed)
                .setContentTitle("Congratulations!")
                .setContentText("You completed your goal: " + goalName)
                .setPriority(NotificationCompat.PRIORITY_HIGH);

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.notify((int) System.currentTimeMillis(), builder.build());
            Log.d("NotificationDebug", "Notification sent successfully");
        } else {
            Log.e("NotificationDebug", "NotificationManager is null");
        }
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Goal Notifications";
            String description = "Channel for goal completion notifications";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }

    private boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.nav_profile:
                startActivity(new Intent(this, UserProfileActivity.class));
                return true;
            case R.id.nav_transactions:
                startActivity(new Intent(this, ExpensesActivity.class));
                return true;
            case R.id.nav_goals:
                return true;
            default:
                return false;
        }
    }
}
