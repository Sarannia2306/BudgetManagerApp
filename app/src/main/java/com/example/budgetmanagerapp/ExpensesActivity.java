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

        // Fetch and display transaction data
        fetchTransactionData();
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

                // Iterate through each transaction
                for (DataSnapshot transactionSnapshot : snapshot.getChildren()) {
                    String type = transactionSnapshot.child("type").getValue(String.class);
                    String amount = transactionSnapshot.child("amount").getValue(String.class);
                    String date = transactionSnapshot.child("date").getValue(String.class);

                    // Calculate total expenses
                    if (amount != null) {
                        totalExpenses += Double.parseDouble(amount);
                    }

                    // Create and customize the TextView for each transaction
                    TextView transactionView = new TextView(ExpensesActivity.this);
                    transactionView.setText(String.format("%s: RM %s on %s", type, amount, date));
                    transactionView.setTextColor(getResources().getColor(R.color.yellowish));
                    transactionView.setTextSize(18);
                    transactionView.setPadding(0, 8, 0, 8); // Add padding for spacing

                    // Add the TextView to the LinearLayout
                    transactionList.addView(transactionView);
                }

                // Display the total expenses
                totalExpensesText.setText("RM " + totalExpenses);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Error fetching transactions", error.toException());
                Toast.makeText(ExpensesActivity.this, "Failed to load expenses", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
