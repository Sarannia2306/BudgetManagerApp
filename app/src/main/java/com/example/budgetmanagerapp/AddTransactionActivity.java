package com.example.budgetmanagerapp;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class AddTransactionActivity extends AppCompatActivity {

    private EditText amountInput, dateInput, descriptionInput;
    private Spinner typeSpinner, categorySpinner;
    private Button saveButton;
    private DatabaseReference transactionDatabaseRef;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_transaction);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        // Check if user is authenticated
        if (currentUser != null) {
            String uid = currentUser.getUid();

            transactionDatabaseRef = FirebaseDatabase.getInstance().getReference("Users").child(uid).child("Transactions");
        } else {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        amountInput = findViewById(R.id.amountInput);
        dateInput = findViewById(R.id.dateInput);
        descriptionInput = findViewById(R.id.descriptionInput);
        typeSpinner = findViewById(R.id.typeSpinner);
        categorySpinner = findViewById(R.id.categorySpinner);
        saveButton = findViewById(R.id.saveButton);
        ImageView calc_2 = findViewById(R.id.calc_2);
        ImageView logoImageView = findViewById(R.id.logoImageView);

        // Save button click listener
        saveButton.setOnClickListener(v -> saveTransaction());

        // Calculator icon click listener
        calc_2.setOnClickListener(v -> {
            Intent calculatorIntent = new Intent(AddTransactionActivity.this, CalculatorActivity.class);
            startActivity(calculatorIntent);
        });

        // ImageView click listener to navigate to HomePageActivity
        logoImageView.setOnClickListener(v -> {
            Intent homeIntent = new Intent(AddTransactionActivity.this, HomePageActivity.class);
            startActivity(homeIntent);
        });

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setOnNavigationItemSelectedListener(this::onNavigationItemSelected);

        // Set date input click listener to show date picker
        dateInput.setOnClickListener(v -> showDatePickerDialog());
    }

    private void saveTransaction() {
        String amount = amountInput.getText().toString().trim();
        String date = dateInput.getText().toString().trim();
        String description = descriptionInput.getText().toString().trim();
        String type = typeSpinner.getSelectedItem().toString();
        String category = categorySpinner.getSelectedItem().toString();

        // Validate input
        if (TextUtils.isEmpty(amount) || TextUtils.isEmpty(date) || TextUtils.isEmpty(description)) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // Fetch the last transaction ID
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = currentUser.getUid();
        DatabaseReference lastIdRef = FirebaseDatabase.getInstance().getReference("Users").child(userId).child("lastTransactionId");

        lastIdRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int lastTransactionId = 0;
                if (snapshot.exists()) {
                    lastTransactionId = snapshot.getValue(Integer.class);
                }

                int newTransactionId = lastTransactionId + 1;

                // Store transaction data
                Map<String, Object> transactionData = new HashMap<>();
                transactionData.put("id", newTransactionId);
                transactionData.put("amount", amount);
                transactionData.put("date", date);
                transactionData.put("description", description);
                transactionData.put("type", type);
                transactionData.put("category", category);

                // Save transaction data to Firebase
                DatabaseReference transactionRef = FirebaseDatabase.getInstance().getReference("Users").child(userId).child("Transactions").child(String.valueOf(newTransactionId));
                transactionRef.setValue(transactionData)
                        .addOnSuccessListener(aVoid -> {

                            lastIdRef.setValue(newTransactionId)
                                    .addOnSuccessListener(aVoid1 -> Log.d("AddTransactionActivity", "Last transaction ID updated successfully"))
                                    .addOnFailureListener(e -> Log.e("AddTransactionActivity", "Failed to update last transaction ID: " + e.getMessage()));
                            Toast.makeText(AddTransactionActivity.this, "Transaction added", Toast.LENGTH_SHORT).show();
                            updateBalance(type, Double.parseDouble(amount)); // Update the balance
                            clearInputs();
                        })
                        .addOnFailureListener(e -> Toast.makeText(AddTransactionActivity.this, "Failed to add transaction: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(AddTransactionActivity.this, "Failed to fetch last transaction ID", Toast.LENGTH_SHORT).show();
                    }
            });
        }

    private void updateBalance(String type, double amount) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = currentUser.getUid();
        DatabaseReference balanceRef = FirebaseDatabase.getInstance().getReference("Users").child(userId).child("balance");

        // Update the balance based on the transaction type
        balanceRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                double currentBalance = snapshot.exists() ? snapshot.getValue(Double.class) : 0.0;
                double newBalance = type.equals("Income") ? currentBalance + amount : currentBalance - amount;

                // Save the new balance back to the database
                balanceRef.setValue(newBalance)
                        .addOnSuccessListener(aVoid -> Log.d("AddTransactionActivity", "Balance updated successfully"))
                        .addOnFailureListener(e -> Log.e("AddTransactionActivity", "Failed to update balance: " + e.getMessage()));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(AddTransactionActivity.this, "Failed to fetch current balance", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void clearInputs() {
        amountInput.setText("");
        dateInput.setText("");
        descriptionInput.setText("");
        typeSpinner.setSelection(0);
        categorySpinner.setSelection(0);
    }

    private boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.nav_goals:
                // Navigate to AddGoalActivity
                Intent goalIntent = new Intent(AddTransactionActivity.this, AddGoalActivity.class);
                startActivity(goalIntent);
                return true;
            case R.id.nav_profile:
                // Navigate to UserProfileActivity
                Intent profileIntent = new Intent(AddTransactionActivity.this, UserProfileActivity.class);
                startActivity(profileIntent);
                return true;
            case R.id.nav_transactions:
                // Stay on AddTransactionActivity
                return true;
            default:
                return false;
        }
    }

    private void showDatePickerDialog() {

        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this, (view, selectedYear, selectedMonth, selectedDay) -> {

            String selectedDate = String.format("%02d/%02d/%d", selectedDay, selectedMonth + 1, selectedYear);
            dateInput.setText(selectedDate);
        }, year, month, day);

        datePickerDialog.show();
    }
}
