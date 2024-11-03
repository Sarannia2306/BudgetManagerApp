package com.example.budgetmanagerapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ExpensesActivity extends AppCompatActivity {

    private TextView totalExpensesText;
    private LinearLayout transactionList;
    private DatabaseReference transactionDatabaseRef;
    private DatabaseReference incomeDatabaseRef;
    private DatabaseReference goalsDatabaseRef;
    private FirebaseAuth mAuth;
    private static final String TAG = "ExpensesActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_expenses);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        // Check if the user is authenticated
        if (currentUser != null) {
            String uid = currentUser.getUid();
            transactionDatabaseRef = FirebaseDatabase.getInstance().getReference("Users").child(uid).child("Transactions");
            incomeDatabaseRef = FirebaseDatabase.getInstance().getReference("Users").child(uid).child("Income");
            goalsDatabaseRef = FirebaseDatabase.getInstance().getReference("Users").child(uid).child("Goals");
        } else {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initialize UI components
        totalExpensesText = findViewById(R.id.totalExpenses);
        transactionList = findViewById(R.id.transactionList);
        ImageView backBtn = findViewById(R.id.backBtn);
        ImageView calc = findViewById(R.id.calc_);

        // Set up the back button to navigate to HomePageActivity
        backBtn.setOnClickListener(v -> navigateToHomePage());

        // Set up the calculator icon to open CalculatorActivity
        calc.setOnClickListener(v -> openCalculator());

        // Fetch and display transaction, income, and goal data
        fetchTransactionData();
        fetchIncomeData();
        fetchGoalsData();
    }

    private void navigateToHomePage() {
        Intent intent = new Intent(ExpensesActivity.this, HomePageActivity.class);
        startActivity(intent);
        finish();
    }

    private void openCalculator() {
        Intent calculatorIntent = new Intent(ExpensesActivity.this, CalculatorActivity.class);
        startActivity(calculatorIntent);
    }

    private void fetchTransactionData() {
        transactionDatabaseRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                double totalExpenses = 0.0;
                transactionList.removeAllViews(); // Clear previous entries

                for (DataSnapshot transactionSnapshot : snapshot.getChildren()) {
                    String type = transactionSnapshot.child("type").getValue(String.class);
                    String amountString = transactionSnapshot.child("amount").getValue(String.class);
                    String date = transactionSnapshot.child("date").getValue(String.class);

                    double amount = 0.0;
                    if (amountString != null) {
                        try {
                            amount = Double.parseDouble(amountString);
                        } catch (NumberFormatException e) {
                            Log.e(TAG, "Invalid amount format: " + amountString, e);
                        }
                    }

                    // Calculate total expenses if type is "Expense"
                    if (type != null && type.equalsIgnoreCase("Expense")) {
                        totalExpenses += amount;
                    }

                    // Create and customize the TextView for each transaction
                    TextView transactionView = new TextView(ExpensesActivity.this);
                    transactionView.setText(String.format("%s: RM %.2f on %s", type, amount, date));
                    transactionView.setTextColor(getResources().getColor(R.color.yellowish));
                    transactionView.setTextSize(18);
                    transactionView.setPadding(2, 8, 2, 8); // Add padding for spacing

                    // Add the TextView to the LinearLayout
                    transactionList.addView(transactionView);
                }

                // Display the total expenses
                totalExpensesText.setText(String.format("RM %.2f", totalExpenses));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Error fetching transactions", error.toException());
                Toast.makeText(ExpensesActivity.this, "Failed to load expenses", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchIncomeData() {
        incomeDatabaseRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot incomeSnapshot : snapshot.getChildren()) {
                    String type = incomeSnapshot.child("type").getValue(String.class);
                    String amount = incomeSnapshot.child("amount").getValue(String.class);
                    String date = incomeSnapshot.child("date").getValue(String.class);

                    // Create and customize the TextView for each income
                    TextView incomeView = new TextView(ExpensesActivity.this);
                    incomeView.setText(String.format("%s: RM %s on %s", type, amount, date));
                    incomeView.setTextColor(getResources().getColor(R.color.primary_dark));
                    incomeView.setTextSize(18);
                    incomeView.setPadding(2, 8, 2, 8);

                    // Add the TextView to the LinearLayout
                    transactionList.addView(incomeView);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Error fetching income", error.toException());
                Toast.makeText(ExpensesActivity.this, "Failed to load income", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchGoalsData() {
        goalsDatabaseRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot goalSnapshot : snapshot.getChildren()) {
                    String goalName = goalSnapshot.child("goalName").getValue(String.class);
                    Double savedAmount = goalSnapshot.child("savedAmount").getValue(Double.class);
                    Double targetAmount = goalSnapshot.child("targetAmount").getValue(Double.class);
                    String deadline = goalSnapshot.child("deadline").getValue(String.class);

                    // Check for null values to avoid exceptions
                    if (goalName != null && savedAmount != null && targetAmount != null && deadline != null) {
                        // Create and customize the TextView for each goal
                        TextView goalView = new TextView(ExpensesActivity.this);
                        goalView.setText(String.format("%s: Saved RM %.2f of RM %.2f, Deadline: %s", goalName, savedAmount, targetAmount, deadline));
                        goalView.setTextColor(getResources().getColor(R.color.background_dark));
                        goalView.setTextSize(18);
                        goalView.setPadding(2, 8, 2, 8);

                        // Add the TextView to the LinearLayout
                        transactionList.addView(goalView);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Error fetching goals", error.toException());
                Toast.makeText(ExpensesActivity.this, "Failed to load goals", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
