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
        ImageView arrowIcon = findViewById(R.id.imageView4);
        ImageView reportIcon = findViewById(R.id.imageView);


        reportIcon.setOnClickListener(v -> {
            Intent reportIntent = new Intent(HomePageActivity.this, ReportActivity.class);
            startActivity(reportIntent);
        });


        // Set up OnClickListener for the arrow icon
        arrowIcon.setOnClickListener(v -> {
            Intent addTransactionIntent = new Intent(HomePageActivity.this, AddTransactionActivity.class);
            startActivity(addTransactionIntent);
        });

        // Goals Overview CardView OnClickListener
        findViewById(R.id.goals_overview_card).setOnClickListener(v -> {
            Intent goalsOverviewIntent = new Intent(HomePageActivity.this, GoalsOverviewActivity.class);
            startActivity(goalsOverviewIntent);
        });

        // Initialize bottom navigation
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setOnNavigationItemSelectedListener(this::onNavigationItemSelected);

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
                    if (balanceValue instanceof Number) {
                        double balance = ((Number) balanceValue).doubleValue();
                        balanceAmount.setText(String.format("RM %.2f", balance));
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
            case R.id.nav_goals:
                startActivity(new Intent(this, AddGoalActivity.class));
                finish();
                return true;
            case R.id.nav_profile:
                startActivity(new Intent(this, UserProfileActivity.class));
                return true;
            case R.id.nav_transactions:
                startActivity(new Intent(this, ExpensesActivity.class));
                return true;
            default:
                return false;
        }
    }
}
