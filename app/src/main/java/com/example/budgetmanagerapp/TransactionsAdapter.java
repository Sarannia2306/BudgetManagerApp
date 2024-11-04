package com.example.budgetmanagerapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class TransactionsAdapter extends RecyclerView.Adapter<TransactionsAdapter.TransactionViewHolder> {

    private List<Transaction> transactions; // List to hold Transaction objects

    // Constructor to initialize the transactions list
    public TransactionsAdapter(List<Transaction> transactions) {
        this.transactions = transactions;
    }

    @NonNull
    @Override
    public TransactionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the layout for each transaction item
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_transaction, parent, false);
        return new TransactionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TransactionViewHolder holder, int position) {
        // Get the transaction for the current position and bind data to the view holder
        Transaction transaction = transactions.get(position);
        holder.bind(transaction);
    }

    @Override
    public int getItemCount() {
        // Return the total number of transactions
        return transactions.size();
    }

    // ViewHolder class to hold and bind transaction data
    public static class TransactionViewHolder extends RecyclerView.ViewHolder {
        private final TextView descriptionText; // TextView for transaction description
        private final TextView amountText;       // TextView for transaction amount
        private final TextView dateText;         // TextView for transaction date

        // Constructor to initialize the views
        public TransactionViewHolder(@NonNull View itemView) {
            super(itemView);
            descriptionText = itemView.findViewById(R.id.description_text);
            amountText = itemView.findViewById(R.id.amount_text);
            dateText = itemView.findViewById(R.id.date_text);
        }

        // Method to bind transaction data to the views
        public void bind(Transaction transaction) {
            descriptionText.setText(transaction.getDescription());
            amountText.setText(String.format("RM %.2f", transaction.getAmount()));
            dateText.setText(String.format("Date: %s", transaction.getDate()));
        }
    }

    // Optional: Method to update the transactions list from outside
    public void setTransactions(List<Transaction> transactions) {
        this.transactions = transactions;
        notifyDataSetChanged(); // Notify the adapter that data has changed
    }
}
