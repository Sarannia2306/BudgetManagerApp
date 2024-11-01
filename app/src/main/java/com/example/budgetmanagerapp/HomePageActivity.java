package com.example.budgetmanagerapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
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

public class HomePageActivity extends AppCompatActivity {

    private TextView balanceAmount;
    private static final String TAG = "HomePageActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_homepage);

        balanceAmount = findViewById(R.id.balance_amount);
        ImageView arrowIcon = findViewById(R.id.imageView4); // Reference the arrow icon ImageView

        // Set up OnClickListener for the arrow icon
        arrowIcon.setOnClickListener(v -> {
            Intent addTransactionIntent = new Intent(HomePageActivity.this, AddTransactionActivity.class);
            startActivity(addTransactionIntent);
        });

        // Initialize bottom navigation
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> onNavigationItemSelected(item));

        fetchBalance();
    }

    private void fetchBalance() {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = auth.getCurrentUser();

        if (currentUser == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = currentUser.getUid();
        DatabaseReference database = FirebaseDatabase.getInstance().getReference("Users").child(userId);

        database.child("balance").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Object balanceValue = snapshot.getValue();
                    if (balanceValue instanceof Long || balanceValue instanceof Double) {
                        double balance = ((Number) balanceValue).doubleValue();
                        balanceAmount.setText(String.format("RM %.2f", balance));
                    } else if (balanceValue instanceof String) {
                        balanceAmount.setText("RM " + balanceValue);
                    } else {
                        balanceAmount.setText("RM 0.00");
                        Log.w(TAG, "Unexpected data type for balance");
                    }
                } else {
                    balanceAmount.setText("RM 0.00");
                    Log.w(TAG, "Balance field not found for user");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(HomePageActivity.this, "Failed to fetch balance", Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Error fetching balance", error.toException());
            }
        });
    }

    private boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.nav_home:
                // Stay on the HomePageActivity
                return true;
            case R.id.nav_profile:
                // Navigate to UserProfileActivity
                Intent profileIntent = new Intent(HomePageActivity.this, UserProfileActivity.class);
                startActivity(profileIntent);
                return true;
            case R.id.nav_transactions:
                Intent transactionIntent = new Intent(HomePageActivity.this, ExpensesActivity.class);
                startActivity(transactionIntent);
                return true;
            default:
                return false;
        }
    }
}
