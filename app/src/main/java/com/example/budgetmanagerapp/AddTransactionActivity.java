package com.example.budgetmanagerapp;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
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

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class AddTransactionActivity extends AppCompatActivity {

    private static final String INSERT_URL = "http://10.0.2.2:8080/budget_manager/insert_transaction.php";

    EditText amountInput, descriptionInput, dateInput;
    Spinner typeSpinner, categorySpinner;
    Button saveButton;
    Calendar calendar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_transaction);

        amountInput = findViewById(R.id.amountInput);
        descriptionInput = findViewById(R.id.descriptionInput);
        dateInput = findViewById(R.id.dateInput);
        typeSpinner = findViewById(R.id.typeSpinner);
        categorySpinner = findViewById(R.id.categorySpinner);
        saveButton = findViewById(R.id.saveButton);
        calendar = Calendar.getInstance();

        // Date picker dialog when clicking on the dateInput field
        dateInput.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int year = calendar.get(Calendar.YEAR);
                int month = calendar.get(Calendar.MONTH);
                int day = calendar.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog datePickerDialog = new DatePickerDialog(AddTransactionActivity.this,
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                                String selectedDate = year + "-" + (monthOfYear + 1) + "-" + dayOfMonth;
                                dateInput.setText(selectedDate);
                            }
                        }, year, month, day);
                datePickerDialog.show();
            }
        });

        // Save the transaction by sending data to MySQL using Volley
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (amountInput.getText().toString().isEmpty() || dateInput.getText().toString().isEmpty()) {
                    Toast.makeText(AddTransactionActivity.this, "Please enter all details", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Get the input values
                final String amount = amountInput.getText().toString();
                final String type = typeSpinner.getSelectedItem().toString();
                final String category = categorySpinner.getSelectedItem().toString();
                final String description = descriptionInput.getText().toString();
                final String date = dateInput.getText().toString();

                // Send data to the server
                sendTransactionToServer(amount, type, category, date, description);
            }
        });
    }

    private void sendTransactionToServer(final String amount, final String type, final String category, final String date, final String description) {
        RequestQueue queue = Volley.newRequestQueue(this);

        StringRequest request = new StringRequest(Request.Method.POST, INSERT_URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    boolean success = jsonObject.getBoolean("success");

                    if (success) {
                        Toast.makeText(AddTransactionActivity.this, "Transaction added", Toast.LENGTH_SHORT).show();
                        finish();  // Return to MainActivity
                    } else {
                        Toast.makeText(AddTransactionActivity.this, "Error adding transaction", Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(AddTransactionActivity.this, "JSON parsing error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(AddTransactionActivity.this, "Volley error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                // Send the transaction data as POST parameters
                Map<String, String> params = new HashMap<>();
                params.put("amount", amount);
                params.put("type", type);
                params.put("category", category);
                params.put("date", date);
                params.put("description", description);
                return params;
            }
        };

        // Add the request to the queue
        queue.add(request);
    }
}
