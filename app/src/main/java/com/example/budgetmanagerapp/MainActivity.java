package com.example.budgetmanagerapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
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

public class MainActivity extends AppCompatActivity {

    private static final String TOTALS_URL = "http://10.0.2.2:8080/budget_manager/get_totals.php"; // Replace with your server URL

    TextView totalIncomeText, totalExpenseText, balanceText;
    Button addTransactionBtn, loginBtn, registerBtn, logoutBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        totalIncomeText = findViewById(R.id.totalIncomeText);
        totalExpenseText = findViewById(R.id.totalExpenseText);
        balanceText = findViewById(R.id.balanceText);
        addTransactionBtn = findViewById(R.id.addTransactionBtn);
        loginBtn = findViewById(R.id.loginBtn);
        registerBtn = findViewById(R.id.registerBtn);
        logoutBtn = findViewById(R.id.logoutBtn); // New logout button

        // Open AddTransactionActivity when the button is clicked
        addTransactionBtn.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AddTransactionActivity.class);
            startActivity(intent);
        });

        // Navigate to LoginActivity when the login button is clicked
        loginBtn.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
        });

        // Navigate to RegisterActivity when the register button is clicked
        registerBtn.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, RegisterActivity.class);
            startActivity(intent);
        });

        // Handle logout button click
        logoutBtn.setOnClickListener(v -> {
            // Here, you could also clear any user session data if necessary
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
            finish(); // Close MainActivity to prevent going back to it after logout
        });

        // Fetch totals from the server when activity is created
        updateDashboard();
    }

    // Refresh the dashboard whenever the activity resumes
    @Override
    protected void onResume() {
        super.onResume();
        updateDashboard();
    }

    // Update the dashboard with income, expenses, and balance
    private void updateDashboard() {
        fetchTotalsFromServer("Income");
        fetchTotalsFromServer("Expense");
    }

    // Method to fetch totals from the server (for either income or expense)
    private void fetchTotalsFromServer(final String type) {
        RequestQueue queue = Volley.newRequestQueue(this);

        // Append type to the URL (Income or Expense)
        StringRequest request = new StringRequest(Request.Method.GET, TOTALS_URL + "?type=" + type, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    double total = jsonObject.getDouble("total");

                    // Set the total income or expense
                    if (type.equals("Income")) {
                        totalIncomeText.setText("Income: RM" + total);
                    } else if (type.equals("Expense")) {
                        totalExpenseText.setText("Expense: RM" + total);
                    }

                    // Calculate and update the balance
                    updateBalance();

                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(MainActivity.this, "JSON parsing error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(MainActivity.this, "Volley error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        queue.add(request);
    }

    // Method to update the balance displayed in the UI
    private void updateBalance() {
        try {
            double totalIncome = Double.parseDouble(totalIncomeText.getText().toString().replace("Income: RM", ""));
            double totalExpense = Double.parseDouble(totalExpenseText.getText().toString().replace("Expense: RM", ""));
            balanceText.setText("Balance: RM" + (totalIncome - totalExpense));
        } catch (NumberFormatException e) {
            // Handle any potential parsing issues gracefully
            balanceText.setText("Balance: RM0");
        }
    }
}
