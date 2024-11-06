package com.example.budgetmanagerapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class AboutUsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about_us);

        Button backToHomeButton = findViewById(R.id.backToHomeButton);

        backToHomeButton.setOnClickListener(view -> {
            Intent intent = new Intent(AboutUsActivity.this, HomePageActivity.class);
            startActivity(intent);
            finish();
        });
    }
}
