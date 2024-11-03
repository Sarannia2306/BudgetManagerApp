package com.example.budgetmanagerapp;

import android.app.AlertDialog;
import android.content.Context;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

public class GoalsAdapter extends RecyclerView.Adapter<GoalsAdapter.GoalViewHolder> {

    private List<Goal> goalsList;
    private FirebaseAuth mAuth;

    // Constructor
    public GoalsAdapter(List<Goal> goalsList) {
        this.goalsList = goalsList;
        mAuth = FirebaseAuth.getInstance();
    }

    @NonNull
    @Override
    public GoalViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the item layout for each goal
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_goal, parent, false);
        return new GoalViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GoalViewHolder holder, int position) {
        Goal goal = goalsList.get(position);

        // Set text values for the goal details
        holder.goalNameTextView.setText(goal.getGoalName() != null ? goal.getGoalName() : "Unnamed Goal");
        holder.targetAmountTextView.setText(String.format("RM %.2f", goal.getTargetAmount()));
        holder.savedAmountTextView.setText(String.format("RM %.2f", goal.getSavedAmount()));
        holder.deadlineTextView.setText(goal.getDeadline() != null ? goal.getDeadline() : "No Deadline");
        holder.statusTextView.setText(goal.getStatus() != null ? goal.getStatus() : "No Status");

        // Determine visibility and update progress bar based on saved amount
        int progress = (goal.getTargetAmount() > 0) ? (int) ((goal.getSavedAmount() / goal.getTargetAmount()) * 100) : 0;
        updateProgressViews(holder.progressBar, progress);

        // Add Amount Button click listener
        holder.addAmountButton.setOnClickListener(v -> showAddAmountDialog(holder.itemView.getContext(), goal));
    }

    private void updateProgressViews(LinearLayout progressBar, int progress) {
        progressBar.findViewById(R.id.progress30).setVisibility(View.GONE);
        progressBar.findViewById(R.id.progress50).setVisibility(View.GONE);
        progressBar.findViewById(R.id.progress100).setVisibility(View.GONE);

        if (progress >= 100) {
            progressBar.findViewById(R.id.progress100).setVisibility(View.VISIBLE);
        } else if (progress >= 50) {
            progressBar.findViewById(R.id.progress50).setVisibility(View.VISIBLE);
        } else if (progress >= 30) {
            progressBar.findViewById(R.id.progress30).setVisibility(View.VISIBLE);
        }
    }

    @Override
    public int getItemCount() {
        return (goalsList != null) ? goalsList.size() : 0;
    }

    // ViewHolder class to hold references to the views in each item
    public static class GoalViewHolder extends RecyclerView.ViewHolder {
        TextView goalNameTextView, targetAmountTextView, savedAmountTextView, deadlineTextView, statusTextView;
        LinearLayout progressBar; // Change to LinearLayout to hold multiple progress views
        Button addAmountButton; // Reference to the add amount button

        public GoalViewHolder(@NonNull View itemView) {
            super(itemView);
            goalNameTextView = itemView.findViewById(R.id.goalNameTextView);
            targetAmountTextView = itemView.findViewById(R.id.targetAmountTextView);
            savedAmountTextView = itemView.findViewById(R.id.savedAmountTextView);
            deadlineTextView = itemView.findViewById(R.id.deadlineTextView);
            statusTextView = itemView.findViewById(R.id.statusTextView);
            progressBar = itemView.findViewById(R.id.progressBar); // Initialize as LinearLayout
            addAmountButton = itemView.findViewById(R.id.addAmountButton); // Initialize the button
        }
    }

    // Method to show the dialog for adding more money to the goal
    private void showAddAmountDialog(Context context, Goal goal) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Add Amount to Goal");

        // Set up the input
        final EditText input = new EditText(context);
        input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        builder.setView(input);

        builder.setPositiveButton("Add", (dialog, which) -> {
            String amountString = input.getText().toString();
            if (!amountString.isEmpty()) {
                double amountToAdd = Double.parseDouble(amountString);
                goal.addContribution(amountToAdd); // Update the saved amount

                // Update Firebase with the new saved amount
                updateGoalInFirebase(goal, context, amountToAdd);
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void updateGoalInFirebase(Goal goal, Context context, double amountToAdd) {
        DatabaseReference goalRef = FirebaseDatabase.getInstance()
                .getReference("Users")
                .child(mAuth.getCurrentUser().getUid())
                .child("Goals")
                .child(goal.getGoalId());

        goalRef.setValue(goal).addOnSuccessListener(aVoid -> {
            // Deduct from the user's balance
            deductFromBalance(goal, amountToAdd, context);
            Toast.makeText(context, "Amount added!", Toast.LENGTH_SHORT).show();
        }).addOnFailureListener(e -> {
            Toast.makeText(context, "Failed to update goal: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    private void deductFromBalance(Goal goal, double amountToAdd, Context context) {
        DatabaseReference userRef = FirebaseDatabase.getInstance()
                .getReference("Users")
                .child(mAuth.getCurrentUser().getUid());

        userRef.child("balance").get().addOnSuccessListener(snapshot -> {
            if (snapshot.exists()) {
                double currentBalance = snapshot.getValue(Double.class);
                if (currentBalance >= amountToAdd) {
                    // Deduct the amount from balance
                    currentBalance -= amountToAdd;
                    userRef.child("balance").setValue(currentBalance).addOnSuccessListener(aVoid -> {
                        // Check if the goal is complete and update status if necessary
                        if (goal.isGoalComplete()) {
                            goal.setStatus("Completed");
                            updateGoalInFirebase(goal, context, 0); // Update goal status
                        }
                    });
                } else {
                    Toast.makeText(context, "Insufficient balance to add this amount!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
