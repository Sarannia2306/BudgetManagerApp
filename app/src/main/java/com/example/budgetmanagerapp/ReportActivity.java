package com.example.budgetmanagerapp;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ReportActivity extends AppCompatActivity {

    private RecyclerView transactionsRecyclerView; // RecyclerView to display transactions
    private TransactionsAdapter transactionsAdapter; // Adapter for the RecyclerView
    private List<Transaction> monthlyTransactions; // List to hold transactions for the selected month
    private Button selectMonthButton; // Button to select month for the report
    private Button selectTypeButton; // Button to select transaction type
    private String selectedType = "All"; // Default type to show all transactions

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report); // Set the layout for this activity

        // Initialize RecyclerView and Adapter
        transactionsRecyclerView = findViewById(R.id.recycler_view);
        transactionsRecyclerView.setLayoutManager(new LinearLayoutManager(this)); // Set layout manager
        monthlyTransactions = new ArrayList<>(); // Initialize the transactions list
        transactionsAdapter = new TransactionsAdapter(monthlyTransactions); // Create the adapter
        transactionsRecyclerView.setAdapter(transactionsAdapter); // Set the adapter to the RecyclerView
        ImageView logoImageView = findViewById(R.id.logoImageView);

        // Logo click listener to navigate to HomePageActivity
        logoImageView.setOnClickListener(v -> {
            startActivity(new Intent(ReportActivity.this, HomePageActivity.class));
        });

        // Initialize Select Month Button
        selectMonthButton = findViewById(R.id.selectMonthButton);
        selectMonthButton.setOnClickListener(v -> showMonthYearPicker()); // Set onClick listener to show date picker

        // Initialize Select Type Button
        selectTypeButton = findViewById(R.id.selectTypeButton);
        selectTypeButton.setOnClickListener(v -> showTypeSelectionDialog()); // Set listener for type selection

        // Load transactions for the current month by default
        Calendar today = Calendar.getInstance();
        loadMonthlyTransactions(today.get(Calendar.YEAR), today.get(Calendar.MONTH) + 1, selectedType);
    }

    private void showMonthYearPicker() {
        Calendar today = Calendar.getInstance();
        int year = today.get(Calendar.YEAR);
        int month = today.get(Calendar.MONTH); // 0-indexed month

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int selectedYear, int selectedMonth, int dayOfMonth) {
                        // Load transactions for the selected month
                        loadMonthlyTransactions(selectedYear, selectedMonth + 1, selectedType);
                    }
                }, year, month, 1); // Show dialog starting from today

        datePickerDialog.show(); // Display the date picker dialog
    }

    private void showTypeSelectionDialog() {
        String[] types = {"All", "Income", "Expenses"}; // Options for transaction types
        new AlertDialog.Builder(this)
                .setTitle("Select Transaction Type")
                .setItems(types, (dialog, which) -> {
                    selectedType = types[which]; // Set the selected type
                    Calendar today = Calendar.getInstance();
                    loadMonthlyTransactions(today.get(Calendar.YEAR), today.get(Calendar.MONTH) + 1, selectedType); // Reload transactions
                })
                .show();
    }

    private void loadMonthlyTransactions(int selectedYear, int selectedMonth, String typeFilter) {
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        if (mAuth.getCurrentUser() == null) {
            Toast.makeText(ReportActivity.this, "User not authenticated.", Toast.LENGTH_SHORT).show();
            return; // Exit if user is not authenticated
        }

        String uid = mAuth.getCurrentUser().getUid(); // Get the current user's ID
        DatabaseReference transactionsRef = FirebaseDatabase.getInstance().getReference("Users")
                .child(uid).child("Transactions"); // Reference to the user's transactions in Firebase

        transactionsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                monthlyTransactions.clear(); // Clear previous transactions

                for (DataSnapshot transactionSnapshot : snapshot.getChildren()) {
                    String dateStr = transactionSnapshot.child("date").getValue(String.class); // Get transaction date

                    // Ensure the date format matches your Firebase data
                    SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                    try {
                        Date date = dateFormat.parse(dateStr); // Parse the date
                        if (date == null) continue; // Skip if date parsing failed

                        Calendar cal = Calendar.getInstance();
                        cal.setTime(date); // Set the parsed date to the calendar

                        int transactionYear = cal.get(Calendar.YEAR); // Get the year of the transaction
                        int transactionMonth = cal.get(Calendar.MONTH) + 1; // Get the month of the transaction

                        // Check if the transaction matches the selected year, month, and type
                        String type = transactionSnapshot.child("type").getValue(String.class);
                        boolean matchesType = typeFilter.equals("All") || typeFilter.equals(type);

                        // Add the transaction if it matches the filters
                        if (transactionYear == selectedYear && transactionMonth == selectedMonth && matchesType) {
                            String description = transactionSnapshot.child("description").getValue(String.class); // Get transaction description
                            String category = transactionSnapshot.child("category").getValue(String.class); // Get transaction category
                            String amountStr = transactionSnapshot.child("amount").getValue(String.class); // Get transaction amount
                            double amount = 0.0;

                            try {
                                amount = Double.parseDouble(amountStr); // Parse the amount
                            } catch (NumberFormatException e) {
                                e.printStackTrace(); // Handle invalid amount format if necessary
                            }

                            // Add the transaction to the list
                            monthlyTransactions.add(new Transaction(dateStr, description, category, type, amount));
                        }

                    } catch (ParseException e) {
                        e.printStackTrace(); // Handle parse exception if necessary
                    }
                }

                transactionsAdapter.notifyDataSetChanged(); // Notify the adapter that the data has changed

                // Show message if no transactions were found for the selected month
                if (monthlyTransactions.isEmpty()) {
                    Toast.makeText(ReportActivity.this, "No transactions found for the selected month.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ReportActivity.this, "Failed to load transactions.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
