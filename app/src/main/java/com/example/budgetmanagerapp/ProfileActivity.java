package com.example.budgetmanagerapp;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.Calendar;

public class ProfileActivity extends AppCompatActivity {

    private EditText firstNameInput, lastNameInput;
    private TextView dobInput;
    private ImageView profileImage;
    private Button uploadImageBtn, saveDetailsBtn;
    private Uri imageUri;
    private DatabaseReference database;
    private StorageReference storageReference;
    private String userId;
    private static final int PICK_IMAGE_REQUEST = 1;
    private static final String TAG = "ProfileActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile); // Ensure you have this layout

        // Initialize Firebase references
        database = FirebaseDatabase.getInstance().getReference("Users");
        storageReference = FirebaseStorage.getInstance().getReference("ProfileImages");

        // Find UI components
        firstNameInput = findViewById(R.id.firstNameInput);
        lastNameInput = findViewById(R.id.lastNameInput);
        dobInput = findViewById(R.id.dobInput);
        profileImage = findViewById(R.id.profileImage);
        uploadImageBtn = findViewById(R.id.uploadImageBtn);
        saveDetailsBtn = findViewById(R.id.saveProfileBtn); // New button for saving details

        // Get userId from intent
        Intent intent = getIntent();
        userId = intent.getStringExtra("USER_ID");

        if (userId == null) {
            Toast.makeText(this, "User ID not found", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "USER_ID not passed to ProfileActivity");
            finish();
            return;
        }

        // Set click listeners
        dobInput.setOnClickListener(view -> showDatePickerDialog());
        uploadImageBtn.setOnClickListener(view -> openFileChooser());
        saveDetailsBtn.setOnClickListener(view -> saveUserDetails());
    }

    private void showDatePickerDialog() {
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    String selectedDate = selectedDay + "-" + (selectedMonth + 1) + "-" + selectedYear;
                    dobInput.setText(selectedDate);
                },
                year, month, day
        );

        datePickerDialog.show();
    }

    private void openFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Profile Image"), PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            profileImage.setImageURI(imageUri);
            Log.d(TAG, "Image selected: " + imageUri.toString());
        }
    }

    private void saveUserDetails() {
        String firstName = firstNameInput.getText().toString().trim();
        String lastName = lastNameInput.getText().toString().trim();
        String dob = dobInput.getText().toString().trim();

        // Validate input fields
        if (firstName.isEmpty() || lastName.isEmpty() || dob.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }
        if (imageUri == null) {
            Toast.makeText(this, "Please select a profile image", Toast.LENGTH_SHORT).show();
            return;
        }

        // Upload profile image and save details
        uploadProfileImageAndSaveDetails(userId, firstName, lastName, dob);
    }

    private void uploadProfileImageAndSaveDetails(String userId, String firstName, String lastName, String dob) {
        StorageReference fileReference = storageReference.child(userId + ".jpg");

        fileReference.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> {
                    Log.d(TAG, "Image upload successful for user: " + userId);
                    fileReference.getDownloadUrl().addOnSuccessListener(uri -> {
                        String imageUrl = uri.toString();
                        Log.d(TAG, "Image URL retrieved: " + imageUrl);

                        // Get current user's email from FirebaseAuth
                        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                        String email = currentUser != null ? currentUser.getEmail() : "No Email";

                        // Create UserProfile object with all user details
                        UserProfile userProfile = new UserProfile(userId, firstName, lastName, dob, email, imageUrl, 0.00);
                        Log.d(TAG, "UserProfile object created: " + userProfile.toString());

                        // Save user details in Firebase Realtime Database
                        database.child(userId).setValue(userProfile)
                                .addOnCompleteListener(task -> {
                                    if (task.isSuccessful()) {
                                        Log.d(TAG, "User profile saved in database successfully.");
                                        Toast.makeText(ProfileActivity.this, "Profile saved successfully!", Toast.LENGTH_SHORT).show();
                                        startActivity(new Intent(ProfileActivity.this, HomePageActivity.class));
                                        finish();
                                    } else {
                                        Log.e(TAG, "Failed to save profile: ", task.getException());
                                        Toast.makeText(ProfileActivity.this, "Failed to save profile: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    Log.e(TAG, "Failed to save profile in database: ", e);
                                    Toast.makeText(ProfileActivity.this, "Failed to save profile in database: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                });
                    }).addOnFailureListener(e -> {
                        Log.e(TAG, "Failed to retrieve image URL: ", e);
                        Toast.makeText(ProfileActivity.this, "Failed to retrieve image URL: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Image upload failed: ", e);
                    Toast.makeText(ProfileActivity.this, "Image upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
