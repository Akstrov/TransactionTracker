package ma.ismagi.cp2.transactiontracker.viewModels;

import android.util.Log;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import ma.ismagi.cp2.transactiontracker.model.Goal;

public class AddGoalViewModel extends ViewModel {
    private final MutableLiveData<Goal> goal = new MutableLiveData<>();
    private final MutableLiveData<Boolean> goalAdded = new MutableLiveData<>();
    private FirebaseAuth auth;
    private FirebaseFirestore db;

    public AddGoalViewModel() {
        goal.setValue(new Goal());
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
    }

    public MutableLiveData<Boolean> getGoalAdded() {
        return goalAdded;
    }

    public MutableLiveData<Goal> getGoal() {
        return goal;
    }

    public void onSubmitClicked() {
        Log.d("AddGoalViewModel", "Submitting goal");
        Goal newGoal = goal.getValue();
        Log.d("AddGoalViewModel", "Submitting goal: " + newGoal);
        if (newGoal != null) {
            String userId = auth.getCurrentUser().getUid();
            newGoal.setCreatedBy(userId);
            // Format the current date as a string (yyyy-MM-dd)
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            String formattedDate = dateFormat.format(new Date());
            newGoal.setCreatedAt(formattedDate);
            db.collection("goals")
                    .add(newGoal)
                    .addOnSuccessListener(documentReference -> {
                        String goalId = documentReference.getId();
                        Log.d("AddGoalViewModel", "Goal added with ID: " + goalId);
                        GoalProgressHelper.fetchAndUpdateCurrentProgress(goalId, newGoal.getTargetAmount(), formattedDate, newGoal.getGoalType());
                        goalAdded.setValue(true);
                    })
                    .addOnFailureListener(e -> {
                        Log.w("AddGoalViewModel", "Error adding goal", e);
                        goalAdded.setValue(false);
                    });
        }
    }

    private void fetchAndUpdateCurrentProgress(String goalId, double targetAmount, String creationDate, String goalType) {
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
                            } else if ("Expense".equals(type)) {
                                totalExpense += amount;
                            }
                        }
                    }

                    double currentProgress = 0.0;
                    double progressPercentage = 0.0;
                    boolean isCompleted = false;

                    if ("Savings".equals(goalType)) {
                        // For savings goal, progress is income minus expenses
                        currentProgress = totalIncome - totalExpense;
                        progressPercentage = (currentProgress / targetAmount) * 100;
                        isCompleted = progressPercentage >= 100;
                    } else if ("Spending".equals(goalType)) {
                        // For spending goal, progress is expenses towards target
                        currentProgress = totalExpense;
                        progressPercentage = (currentProgress / targetAmount) * 100;
                        isCompleted = progressPercentage >= 100;
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


    public void setTargetAmount(String amount) {
        goal.getValue().setTargetAmount(Double.parseDouble(amount));
    }
}
