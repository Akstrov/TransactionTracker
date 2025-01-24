package ma.ismagi.cp2.transactiontracker.viewModels;

import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class GoalProgressHelper {
    private static FirebaseAuth auth;
    private static FirebaseFirestore db;

    static {
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
    }

    public static void fetchAndUpdateCurrentProgress(String goalId, double targetAmount, String creationDate, String goalType) {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) return;
        String userId = user.getUid();
        Log.d("AddGoalViewModel", "Fetching transactions for user: " + userId);

        db.collection("transactions")
                .whereEqualTo("createdBy", userId)
                .whereGreaterThanOrEqualTo("date", creationDate) // Fetch transactions from today onward
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    Log.d("AddGoalViewModel", "Found " + querySnapshot.size() + " transactions.");
                    double totalIncome = 0.0;
                    double totalExpense = 0.0;

                    for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                        Double amount = document.getDouble("amount");
                        String type = document.getString("type"); // "Income" or "Expense"

                        if (amount != null) {
                            if ("Income".equals(type)) {
                                totalIncome += amount;
                                Log.d("AddGoalViewModel", "Income found: " + amount);
                            } else if ("Expense".equals(type)) {
                                totalExpense += amount;
                                Log.d("AddGoalViewModel", "Expense found: " + amount);
                            }
                        }
                    }
                    Log.d("AddGoalViewModel", "Total income: " + totalIncome);
                    Log.d("AddGoalViewModel", "Total expense: " + totalExpense);
                    double currentProgress = 0.0;
                    double progressPercentage = 0.0;
                    boolean isCompleted = false;

                    if ("Saving".equals(goalType)) {
                        // For savings goal, progress is income minus expenses
                        currentProgress = totalIncome - totalExpense;
                        progressPercentage = (currentProgress / targetAmount) * 100;
                        isCompleted = progressPercentage >= 100;
                        Log.d("AddGoalViewModel", "Current progress Saving: " + currentProgress);

                    } else if ("Spending".equals(goalType)) {
                        // For spending goal, progress is expenses towards target
                        currentProgress = totalExpense;
                        progressPercentage = (currentProgress / targetAmount) * 100;
                        isCompleted = progressPercentage >= 100;
                        Log.d("AddGoalViewModel", "Current progress Spending: " + currentProgress);
                    }

                    Log.d("AddGoalViewModel", "Is goal completed?: " + isCompleted);

                    // Update the goal document with the calculated progress
                    db.collection("goals").document(goalId)
                            .update(
                                    "currentProgress", currentProgress,
                                    "progressPercentage", progressPercentage,
                                    "completed", isCompleted
                            )
                            .addOnSuccessListener(unused -> Log.d("AddGoalViewModel", "Current progress updated"))
                            .addOnFailureListener(e -> Log.w("AddGoalViewModel", "Error updating progress", e));
                })
                .addOnFailureListener(e -> Log.w("AddGoalViewModel", "Error fetching transactions", e));
    }

}
