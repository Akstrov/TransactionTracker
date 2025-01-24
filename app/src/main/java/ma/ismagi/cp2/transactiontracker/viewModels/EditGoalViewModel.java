package ma.ismagi.cp2.transactiontracker.viewModels;

import android.util.Log;

import androidx.databinding.ObservableField;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import ma.ismagi.cp2.transactiontracker.model.Goal;

public class EditGoalViewModel extends ViewModel {
    private final MutableLiveData<Goal> goal = new MutableLiveData<>();
    private final MutableLiveData<Boolean> goalUpdated = new MutableLiveData<>();
    private FirebaseFirestore db;
    private FirebaseAuth auth;

    public EditGoalViewModel() {
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        goal.setValue(new Goal());
    }

    public MutableLiveData<Goal> getGoal() {
        return goal;
    }
    public void setGoal(Goal goal) {
        this.goal.setValue(goal);
    }
    public void onSaveClicked() {
        Log.d("EditGoalViewModel", "onSaveClicked called: " + goal.getValue());
        db.collection("goals")
                .document(goal.getValue().getId())
                .set(goal.getValue())
                .addOnSuccessListener(v -> {
                    Log.d("EditGoalViewModel", "Goal updated successfully");
                    goalUpdated.setValue(true);
                })
                .addOnFailureListener(e -> {
                    Log.w("EditGoalViewModel", "Error updating goal", e);
                    goalUpdated.setValue(false);
                });
    }

    public MutableLiveData<Boolean> getGoalUpdated() {
        return goalUpdated;
    }
    public void setTargetAmount(String amount) {
        try {
            // Ensure that the input is parsed as a Double
            double parsedAmount = Double.parseDouble(amount);
            goal.getValue().setTargetAmount(parsedAmount);
        } catch (NumberFormatException e) {
            Log.w("AddGoalViewModel", "Invalid amount format", e);
        }
    }

}
