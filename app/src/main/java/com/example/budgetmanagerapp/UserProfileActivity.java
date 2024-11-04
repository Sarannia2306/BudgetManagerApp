package com.example.budgetmanagerapp;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class UserProfileActivity extends AppCompatActivity {

    private EditText firstNameInput, lastNameInput, dobInput, emailInput;
    private ImageView profileImage;
    private Button signOutButton, updateButton;
    private FirebaseAuth mAuth;
    private DatabaseReference userDatabaseRef;
    private static final String TAG = "UserProfileActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_profile);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            userDatabaseRef = FirebaseDatabase.getInstance().getReference("Users").child(currentUser.getUid());
        } else {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initialize UI components
        firstNameInput = findViewById(R.id.firstName);
        lastNameInput = findViewById(R.id.lastName);
        dobInput = findViewById(R.id.dob);
        emailInput = findViewById(R.id.email);
        profileImage = findViewById(R.id.profileImage);
        signOutButton = findViewById(R.id.signOutButton);
        updateButton = findViewById(R.id.updateButton);
        ImageView logoImageView = findViewById(R.id.logoImageView);

        // Fetch and display user profile data
        fetchUserProfile();

        // Logo click listener to navigate to HomePageActivity
        logoImageView.setOnClickListener(v -> {
            startActivity(new Intent(UserProfileActivity.this, HomePageActivity.class));
        });

        // Bottom navigation setup
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setOnNavigationItemSelectedListener(this::onNavigationItemSelected);

        // Sign out button click listener
        signOutButton.setOnClickListener(v -> signOutUser());
        // Update button click listener
        updateButton.setOnClickListener(v -> updateUserProfile());
    }

    private void fetchUserProfile() {
        userDatabaseRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                firstNameInput.setText(task.getResult().child("firstName").getValue(String.class));
                lastNameInput.setText(task.getResult().child("lastName").getValue(String.class));
                dobInput.setText(task.getResult().child("dob").getValue(String.class));
                emailInput.setText(task.getResult().child("email").getValue(String.class));
                String imageUrl = task.getResult().child("imageUrl").getValue(String.class);

                if (imageUrl != null && !imageUrl.isEmpty()) {
                    // Load the image asynchronously
                    new ImageLoadTask(imageUrl, profileImage).execute();
                } else {
                    profileImage.setImageResource(R.drawable.baseline_photo);
                }
            } else {
                Toast.makeText(UserProfileActivity.this, "Profile not found", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(e -> {
            Log.e(TAG, "Error fetching profile data", e);
            Toast.makeText(UserProfileActivity.this, "Failed to fetch profile data", Toast.LENGTH_SHORT).show();
        });
    }

    private void updateUserProfile() {
        // Get updated values from the EditText fields
        String updatedFirstName = firstNameInput.getText().toString().trim();
        String updatedLastName = lastNameInput.getText().toString().trim();
        String updatedDob = dobInput.getText().toString().trim();
        String updatedEmail = emailInput.getText().toString().trim();

        // Validate inputs
        if (updatedFirstName.isEmpty() || updatedLastName.isEmpty() || updatedDob.isEmpty() || updatedEmail.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // Update the profile data in Firebase
        userDatabaseRef.child("firstName").setValue(updatedFirstName);
        userDatabaseRef.child("lastName").setValue(updatedLastName);
        userDatabaseRef.child("dob").setValue(updatedDob);
        userDatabaseRef.child("email").setValue(updatedEmail).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(this, "Profile updated successfully", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Failed to update profile", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(e -> {
            Log.e(TAG, "Error updating profile", e);
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    private void signOutUser() {
        mAuth.signOut();
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }

    private boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.nav_goals:
                startActivity(new Intent(this, AddGoalActivity.class));
                finish();
                return true;
            case R.id.nav_profile:
                return true;
            case R.id.nav_transactions:
                startActivity(new Intent(this, ExpensesActivity.class));
                finish();
                return true;
            default:
                return false;
        }
    }

    // Inner class for loading images asynchronously
    private static class ImageLoadTask extends AsyncTask<Void, Void, Bitmap> {
        private final String url;
        private final ImageView imageView;

        public ImageLoadTask(String url, ImageView imageView) {
            this.url = url;
            this.imageView = imageView;
        }

        @Override
        protected Bitmap doInBackground(Void... voids) {
            try {
                URL urlConnection = new URL(url);
                HttpURLConnection connection = (HttpURLConnection) urlConnection.openConnection();
                connection.setDoInput(true);
                connection.connect();
                InputStream input = connection.getInputStream();
                return BitmapFactory.decodeStream(input);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            if (bitmap != null) {
                imageView.setImageBitmap(bitmap);
            } else {
                imageView.setImageResource(R.drawable.baseline_photo);
            }
        }
    }
}
