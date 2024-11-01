package com.example.budgetmanagerapp;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Button;
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

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class UserProfileActivity extends AppCompatActivity {

    private TextView firstNameDisplay, lastNameDisplay, dobDisplay, emailDisplay;
    private ImageView profileImage;
    private Button signOutButton;
    private FirebaseAuth mAuth;
    private DatabaseReference userDatabaseRef;
    private static final String TAG = "UserProfileActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_profile);

        // Initialize Firebase Auth and Database reference
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
        firstNameDisplay = findViewById(R.id.firstName);
        lastNameDisplay = findViewById(R.id.lastName);
        dobDisplay = findViewById(R.id.dob);
        emailDisplay = findViewById(R.id.email);
        profileImage = findViewById(R.id.profileImage);
        signOutButton = findViewById(R.id.signOutButton);

        // Fetch and display user profile details
        fetchUserProfile();

        // Set up BottomNavigationView for navigation
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setOnNavigationItemSelectedListener(this::onNavigationItemSelected);

        // Set up sign-out functionality
        signOutButton.setOnClickListener(v -> signOutUser());
    }

    private void fetchUserProfile() {
        userDatabaseRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String firstName = snapshot.child("firstName").getValue(String.class);
                    String lastName = snapshot.child("lastName").getValue(String.class);
                    String dob = snapshot.child("dob").getValue(String.class);
                    String email = snapshot.child("email").getValue(String.class);
                    String imageUrl = snapshot.child("imageUrl").getValue(String.class);

                    // Display retrieved data
                    firstNameDisplay.setText(firstName);
                    lastNameDisplay.setText(lastName);
                    dobDisplay.setText(dob);
                    emailDisplay.setText(email);

                    // Load profile image from URL
                    if (imageUrl != null && !imageUrl.isEmpty()) {
                        new ImageLoadTask(imageUrl, profileImage).execute();
                    } else {
                        profileImage.setImageResource(R.drawable.baseline_photo); // Default image if no URL
                    }
                } else {
                    Toast.makeText(UserProfileActivity.this, "Profile not found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Error fetching profile data", error.toException());
                Toast.makeText(UserProfileActivity.this, "Failed to fetch profile data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void signOutUser() {
        mAuth.signOut();
        Intent intent = new Intent(UserProfileActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    // Navigation item selection handler
    private boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.nav_home:
                // Navigate to HomePageActivity
                Intent homeIntent = new Intent(UserProfileActivity.this, HomePageActivity.class);
                startActivity(homeIntent);
                finish();
                return true;
            case R.id.nav_profile:
                // Stay on UserProfileActivity
                return true;
            case R.id.nav_transactions:
                // Navigate to ExpensesActivity
                Intent transactionIntent = new Intent(UserProfileActivity.this, ExpensesActivity.class);
                startActivity(transactionIntent);
                finish();
                return true;
            default:
                return false;
        }
    }

    // AsyncTask to load the image from the URL in the background
    private static class ImageLoadTask extends AsyncTask<Void, Void, Bitmap> {
        private String url;
        private ImageView imageView;

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
                imageView.setImageBitmap(getCircularBitmap(bitmap)); // Set circular bitmap
            } else {
                imageView.setImageResource(R.drawable.baseline_photo); // Default image if loading fails
            }
        }

        // Method to create a circular bitmap
        private Bitmap getCircularBitmap(Bitmap bitmap) {
            Bitmap output = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(output);
            final Paint paint = new Paint();
            final Path path = new Path();
            final int color = 0xff424242; // Background color for the circular image
            paint.setAntiAlias(true);
            path.addCircle(bitmap.getWidth() / 2, bitmap.getHeight() / 2, Math.min(bitmap.getWidth(), bitmap.getHeight()) / 2, Path.Direction.CCW);
            canvas.clipPath(path);
            canvas.drawBitmap(bitmap, 0, 0, paint);
            return output;
        }
    }
}
