package ma.ismagi.cp2.transactiontracker.viewModels;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

import ma.ismagi.cp2.transactiontracker.adapter.GoalAdapter;
import ma.ismagi.cp2.transactiontracker.model.Goal;
import ma.ismagi.cp2.transactiontracker.model.Summary;

public class HomeViewModel extends ViewModel {
    private final MutableLiveData<List<Goal>> goalsList = new MutableLiveData<>();
    private final MutableLiveData<Summary> summaryData = new MutableLiveData<>();
    public final GoalAdapter goalsAdapter;

    public HomeViewModel() {
        goalsAdapter = new GoalAdapter(new ArrayList<>());
    }

    public LiveData<List<Goal>> getGoalsList() {
        return goalsList;
    }

    public LiveData<Summary> getSummaryData() {
        return summaryData;
    }

    public void fetchGoals(String userId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("goals")
                .whereEqualTo("createdBy", userId)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<Goal> goals = new ArrayList<>();
                    for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                        Goal goal = document.toObject(Goal.class);
                        if (goal != null) {
                            goals.add(goal);
                            Log.d("HomeViewModel", "Goal fetched: " + goal.getDescription()); // Log each goal
                        }
                    }
                    goalsList.setValue(goals);
                    Log.d("HomeViewModel", "Goals count: " + goals.size()); // Log total goals
                })
                .addOnFailureListener(e -> {
                    Log.e("HomeViewModel", "Error fetching goals", e);
                });
    }

    public void fetchSummary(String userId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("transactions")
                .whereEqualTo("createdBy", userId)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    double totalIncome = 0.0;
                    double totalExpense = 0.0;
                    for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                        Double amount = document.getDouble("amount");
                        String type = document.getString("type");
                        if (amount != null) {
                            if ("Income".equals(type)) {
                                totalIncome += amount;
                            } else if ("Expense".equals(type)) {
                                totalExpense += amount;
                            }
                        }
                    }
                    Summary summary = new Summary(totalIncome, totalExpense);
                    summaryData.setValue(summary); // Update LiveData
                })
                .addOnFailureListener(e -> {
                    Log.e("HomeViewModel", "Error fetching summary", e);
                    summaryData.setValue(new Summary(0.0, 0.0)); // Fallback to default values
                });
    }
    public String getFormattedIncome() {
        Summary summary = summaryData.getValue();
        return summary != null ? String.format("Income: $%.2f", summary.getTotalIncome()) : "Income: $0.00";
    }

    public String getFormattedExpense() {
        Summary summary = summaryData.getValue();
        return summary != null ? String.format("Expenses: $%.2f", summary.getTotalExpense()) : "Expenses: $0.00";
    }
}
