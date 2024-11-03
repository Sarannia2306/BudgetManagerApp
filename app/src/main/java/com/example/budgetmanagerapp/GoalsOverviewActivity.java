package com.example.budgetmanagerapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class GoalsOverviewActivity extends AppCompatActivity {

    private RecyclerView goalsRecyclerView;
    private GoalsAdapter goalsAdapter;
    private List<Goal> goalsList;
    private DatabaseReference goalsDatabaseRef;
    private FirebaseAuth mAuth;
    private ProgressBar progressBar;
    private static final String TAG = "GoalsOverviewActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_goals_overview);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        // Check if user is authenticated
        if (currentUser != null) {
            String uid = currentUser.getUid();
            goalsDatabaseRef = FirebaseDatabase.getInstance().getReference("Users").child(uid).child("Goals");
        } else {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initialize UI elements
        goalsRecyclerView = findViewById(R.id.goalsRecyclerView);
        progressBar = findViewById(R.id.progressBar); // Ensure you add this ProgressBar in your layout
        goalsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        goalsList = new ArrayList<>();
        goalsAdapter = new GoalsAdapter(goalsList);
        goalsRecyclerView.setAdapter(goalsAdapter);
        ImageView logoImageView = findViewById(R.id.logoImageView);

        // Fetch goals from Firebase
        fetchGoalsFromFirebase();

        // ImageView click listener to navigate to HomePageActivity
        logoImageView.setOnClickListener(v -> {
            Intent homeIntent = new Intent(GoalsOverviewActivity.this, HomePageActivity.class);
            startActivity(homeIntent);
        });

        // Set up BottomNavigationView for navigation
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setOnNavigationItemSelectedListener(this::onNavigationItemSelected);
    }

    private void fetchGoalsFromFirebase() {
        progressBar.setVisibility(View.VISIBLE); // Show ProgressBar while loading data
        goalsDatabaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                goalsList.clear(); // Clear the list before adding updated data
                for (DataSnapshot goalSnapshot : snapshot.getChildren()) {
                    Goal goal = goalSnapshot.getValue(Goal.class);
                    if (goal != null) {
                        goalsList.add(goal);
                    }
                }
                goalsAdapter.notifyDataSetChanged(); // Notify the adapter of data changes
                progressBar.setVisibility(View.GONE); // Hide ProgressBar after data is loaded
                Log.d(TAG, "Goals data loaded successfully");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                progressBar.setVisibility(View.GONE); // Hide ProgressBar on error
                Toast.makeText(GoalsOverviewActivity.this, "Failed to fetch goals: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Error fetching goals: ", error.toException());
            }
        });
    }

    private boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.nav_goals:
                Intent goalIntent = new Intent(GoalsOverviewActivity.this, AddGoalActivity.class);
                startActivity(goalIntent);
                return true;
            case R.id.nav_profile:
                Intent profileIntent = new Intent(GoalsOverviewActivity.this, UserProfileActivity.class);
                startActivity(profileIntent);
                return true;
            case R.id.nav_transactions:
                Intent transactionsIntent = new Intent(GoalsOverviewActivity.this, ExpensesActivity.class);
                startActivity(transactionsIntent);
                return true;
            default:
                return false;
        }
    }
}
