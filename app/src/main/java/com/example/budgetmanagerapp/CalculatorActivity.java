package com.example.budgetmanagerapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

public class CalculatorActivity extends AppCompatActivity {

    private EditText display;
    private double result = 0;
    private char currentOperation = ' ';
    private boolean isNewOperation = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calculator);

        display = findViewById(R.id.display);
        ImageView backBtn = findViewById(R.id.backBtn);

        setNumberButtonClickListener(R.id.btn0, "0");
        setNumberButtonClickListener(R.id.btn1, "1");
        setNumberButtonClickListener(R.id.btn2, "2");
        setNumberButtonClickListener(R.id.btn3, "3");
        setNumberButtonClickListener(R.id.btn4, "4");
        setNumberButtonClickListener(R.id.btn5, "5");
        setNumberButtonClickListener(R.id.btn6, "6");
        setNumberButtonClickListener(R.id.btn7, "7");
        setNumberButtonClickListener(R.id.btn8, "8");
        setNumberButtonClickListener(R.id.btn9, "9");

        setOperationButtonClickListener(R.id.btnAdd, '+');
        setOperationButtonClickListener(R.id.btnSubtract, '-');
        setOperationButtonClickListener(R.id.btnMultiply, '*');
        setOperationButtonClickListener(R.id.btnDivide, '/');

        findViewById(R.id.btnEquals).setOnClickListener(v -> {
            calculate();
            display.setText(String.valueOf(result));
            isNewOperation = true;
        });

        // Clear button
        findViewById(R.id.btnClear).setOnClickListener(v -> {
            display.setText("");
            result = 0;
            currentOperation = ' ';
            isNewOperation = true;
        });

        backBtn.setOnClickListener(v -> {
            Intent intent = new Intent(CalculatorActivity.this, HomePageActivity.class);
            startActivity(intent);
            finish();
        });
    }

    private void setNumberButtonClickListener(int buttonId, String value) {
        findViewById(buttonId).setOnClickListener(v -> {
            if (isNewOperation) {
                display.setText(value);
                isNewOperation = false;
            } else {
                display.append(value);
            }
        });
    }

    private void setOperationButtonClickListener(int buttonId, char operation) {
        findViewById(buttonId).setOnClickListener(v -> {
            if (!display.getText().toString().isEmpty()) {
                if (currentOperation != ' ') {
                    calculate();
                } else {
                    result = Double.parseDouble(display.getText().toString());
                }
            }
            currentOperation = operation;
            isNewOperation = true;
        });
    }

    private void calculate() {
        if (!display.getText().toString().isEmpty()) {
            double secondValue = Double.parseDouble(display.getText().toString());
            switch (currentOperation) {
                case '+':
                    result += secondValue;
                    break;
                case '-':
                    result -= secondValue;
                    break;
                case '*':
                    result *= secondValue;
                    break;
                case '/':
                    if (secondValue != 0) {
                        result /= secondValue;
                    } else {
                        display.setText("Error");
                        return;
                    }
                    break;
            }
        }
    }
}
