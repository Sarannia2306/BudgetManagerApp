import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class UserProfileActivity extends AppCompatActivity {

    private TextView profileName, usernameDisplay, firstNameDisplay, lastNameDisplay, dobDisplay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile); // Ensure this is your display layout XML

        // Initialize the TextViews
        profileName = findViewById(R.id.profile_name);
        usernameDisplay = findViewById(R.id.username);
        firstNameDisplay = findViewById(R.id.first_name);
        lastNameDisplay = findViewById(R.id.last_name);
        dobDisplay = findViewById(R.id.dob);

        // Get the data passed from MainActivity
        Intent intent = getIntent();
        String username = intent.getStringExtra("USERNAME");
        String firstName = intent.getStringExtra("FIRST_NAME");
        String lastName = intent.getStringExtra("LAST_NAME");
        String dob = intent.getStringExtra("DOB");

        // Set the data in the TextViews
        profileName.setText(firstName + " " + lastName);
        usernameDisplay.setText(username);
        firstNameDisplay.setText(firstName);
        lastNameDisplay.setText(lastName);
        dobDisplay.setText(dob);
    }
}
