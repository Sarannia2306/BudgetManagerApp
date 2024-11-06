package com.example.budgetmanagerapp;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.os.Bundle;
import android.os.Environment;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ReportActivity extends AppCompatActivity {

    private RecyclerView transactionsRecyclerView;
    private TransactionsAdapter transactionsAdapter;
    private List<Transaction> monthlyTransactions;
    private Button selectMonthButton, selectTypeButton;
    private ImageView logoImageView, generatePdfIcon;
    private String selectedType = "All";

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);


        transactionsRecyclerView = findViewById(R.id.recycler_view);
        transactionsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        monthlyTransactions = new ArrayList<>();
        transactionsAdapter = new TransactionsAdapter(monthlyTransactions);
        transactionsRecyclerView.setAdapter(transactionsAdapter);
        logoImageView = findViewById(R.id.logoImageView);
        selectMonthButton = findViewById(R.id.selectMonthButton);
        selectTypeButton = findViewById(R.id.selectTypeButton);
        generatePdfIcon = findViewById(R.id.pdf_icon);


        logoImageView.setOnClickListener(v -> startActivity(new Intent(ReportActivity.this, HomePageActivity.class)));
        selectMonthButton.setOnClickListener(v -> showMonthYearPicker());
        selectTypeButton.setOnClickListener(v -> showTypeSelectionDialog());
        generatePdfIcon.setOnClickListener(v -> generatePdfReport());

        // Load transactions for the current month by default
        Calendar today = Calendar.getInstance();
        loadMonthlyTransactions(today.get(Calendar.YEAR), today.get(Calendar.MONTH) + 1, selectedType);
    }

    private void showMonthYearPicker() {
        Calendar today = Calendar.getInstance();
        int year = today.get(Calendar.YEAR);
        int month = today.get(Calendar.MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, selectedYear, selectedMonth, dayOfMonth) -> loadMonthlyTransactions(selectedYear, selectedMonth + 1, selectedType),
                year, month, 1);
        datePickerDialog.show();
    }

    private void showTypeSelectionDialog() {
        String[] types = {"All", "Income", "Expenses"};
        new AlertDialog.Builder(this)
                .setTitle("Select Transaction Type")
                .setItems(types, (dialog, which) -> {
                    selectedType = types[which];
                    Calendar today = Calendar.getInstance();
                    loadMonthlyTransactions(today.get(Calendar.YEAR), today.get(Calendar.MONTH) + 1, selectedType);
                })
                .show();
    }

    private void loadMonthlyTransactions(int selectedYear, int selectedMonth, String typeFilter) {
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        if (mAuth.getCurrentUser() == null) {
            Toast.makeText(ReportActivity.this, "User not authenticated.", Toast.LENGTH_SHORT).show();
            return;
        }

        String uid = mAuth.getCurrentUser().getUid();
        DatabaseReference transactionsRef = FirebaseDatabase.getInstance().getReference("Users")
                .child(uid).child("Transactions");

        transactionsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                monthlyTransactions.clear();
                for (DataSnapshot transactionSnapshot : snapshot.getChildren()) {
                    String dateStr = transactionSnapshot.child("date").getValue(String.class);

                    SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                    try {
                        Date date = dateFormat.parse(dateStr);
                        if (date == null) continue;

                        Calendar cal = Calendar.getInstance();
                        cal.setTime(date);

                        int transactionYear = cal.get(Calendar.YEAR);
                        int transactionMonth = cal.get(Calendar.MONTH) + 1;

                        String type = transactionSnapshot.child("type").getValue(String.class);
                        boolean matchesType = typeFilter.equals("All") || typeFilter.equals(type);

                        if (transactionYear == selectedYear && transactionMonth == selectedMonth && matchesType) {
                            String description = transactionSnapshot.child("description").getValue(String.class);
                            String category = transactionSnapshot.child("category").getValue(String.class);
                            String amountStr = transactionSnapshot.child("amount").getValue(String.class);
                            double amount = 0.0;

                            try {
                                amount = Double.parseDouble(amountStr);
                            } catch (NumberFormatException e) {
                                e.printStackTrace();
                            }
                            monthlyTransactions.add(new Transaction(dateStr, description, category, type, amount));
                        }

                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }

                transactionsAdapter.notifyDataSetChanged();
                if (monthlyTransactions.isEmpty()) {
                    Toast.makeText(ReportActivity.this, "No transactions found for the selected month.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ReportActivity.this, "Failed to load transactions.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void generatePdfReport() {
        PdfDocument pdfDocument = new PdfDocument();
        Paint paint = new Paint();
        int pageNumber = 1;

        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(595, 842, pageNumber).create();
        PdfDocument.Page page = pdfDocument.startPage(pageInfo);
        Canvas canvas = page.getCanvas();

        int yPosition = 50;
        paint.setTextSize(18);
        paint.setFakeBoldText(true);
        canvas.drawText("Monthly Transactions Report", 50, yPosition, paint);

        paint.setTextSize(12);
        paint.setFakeBoldText(false);
        yPosition += 40;
        canvas.drawText("Date          Description       Category        Type        Amount", 50, yPosition, paint);

        yPosition += 30;
        paint.setTextSize(10);

        for (Transaction transaction : monthlyTransactions) {
            yPosition += 20;
            String transactionLine = transaction.getDate() + "    " +
                    transaction.getDescription() + "    " +
                    transaction.getCategory() + "    " +
                    transaction.getType() + "    " +
                    transaction.getAmount();
            canvas.drawText(transactionLine, 50, yPosition, paint);

            if (yPosition > 750) {
                pdfDocument.finishPage(page);
                pageInfo = new PdfDocument.PageInfo.Builder(595, 842, ++pageNumber).create();
                page = pdfDocument.startPage(pageInfo);
                canvas = page.getCanvas();
                yPosition = 50;
            }
        }

        pdfDocument.finishPage(page);

    File directory = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), "BudgetReports");
        if (!directory.exists()) {
            directory.mkdirs();
        }

        File file = new File(directory, "MonthlyTransactionsReport.pdf");
        try {
            pdfDocument.writeTo(new FileOutputStream(file));
            Toast.makeText(this, "PDF Report generated and saved to " + file.getPath(), Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error generating PDF: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }

        pdfDocument.close();
    }
}
