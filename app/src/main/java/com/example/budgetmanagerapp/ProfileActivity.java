package com.example.budgetmanagerapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.HashMap;
import java.util.Map;

public class ProfileActivity extends AppCompatActivity {

    private static final String UPDATE_PROFILE_URL = "http://10.0.2.2:8080/budget_manager/update_profile.php";

    private EditText usernameInput, firstNameInput, lastNameInput, dobInput;
    private Button completeProfileBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        usernameInput = findViewById(R.id.usernameInput);
        firstNameInput = findViewById(R.id.firstNameInput);
        lastNameInput = findViewById(R.id.lastNameInput);
        dobInput = findViewById(R.id.dobInput);
        completeProfileBtn = findViewById(R.id.completeProfileBtn);
        completeProfileBtn.setOnClickListener(v -> {
            String username = usernameInput.getText().toString();
            String firstName = firstNameInput.getText().toString();
            String lastName = lastNameInput.getText().toString();
            String dob = dobInput.getText().toString();

            if (!username.isEmpty() && !firstName.isEmpty() && !lastName.isEmpty() && !dob.isEmpty()) {
                updateUserProfile(username, firstName, lastName, dob);
            } else {
                Toast.makeText(ProfileActivity.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateUserProfile(final String username, final String firstName, final String lastName, final String dob) {
        RequestQueue queue = Volley.newRequestQueue(this);

        StringRequest request = new StringRequest(Request.Method.POST, UPDATE_PROFILE_URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    boolean success = jsonObject.getBoolean("success");

                    if (success) {
                        Toast.makeText(ProfileActivity.this, "Profile updated successfully!", Toast.LENGTH_SHORT).show();
                        // Optionally navigate to UserProfileActivity where details will be shown
                        Intent intent = new Intent(ProfileActivity.this, ProfileActivity.class);
                        intent.putExtra("username", username);
                        intent.putExtra("firstName", firstName);
                        intent.putExtra("lastName", lastName);
                        intent.putExtra("dob", dob);
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(ProfileActivity.this, jsonObject.getString("message"), Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(ProfileActivity.this, "JSON parsing error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(ProfileActivity.this, "Volley error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("username", username);
                params.put("firstName", firstName);
                params.put("lastName", lastName);
                params.put("dob", dob);
                return params;
            }
        };

        queue.add(request);
    }
}
