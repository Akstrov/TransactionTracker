package ma.ismagi.cp2.transactiontracker.viewModels;

import android.util.Log;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

import ma.ismagi.cp2.transactiontracker.model.Goal;

public class GoalViewModel extends ViewModel {
    private final MutableLiveData<List<Goal>> goals = new MutableLiveData<>();
    private FirebaseAuth auth;
    private FirebaseFirestore db;

    public GoalViewModel() {
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        loadGoals();
//        loadSampleGoals();
    }
    public MutableLiveData<List<Goal>> getGoals() {
        return goals;
    }
    public void loadSampleGoals() {
        ArrayList<Goal> sampleGoals = new ArrayList<>();
        Goal goal1 = new Goal("savings", 1000.0, 500.0, "Sample Goal 1", "2023-12-31", "user123");
        Goal goal2 = new Goal("spending", 500.0, 200.0, "Sample Goal 2", "2023-11-30", "user123");
        Goal goal3 = new Goal("savings", 2000.0, 1000.0, "Sample Goal 3", "2023-12-15", "user123");
        sampleGoals.add(goal1);
        sampleGoals.add(goal2);
        sampleGoals.add(goal3);
        goals.setValue(sampleGoals);
    }
public void loadGoals() {
    FirebaseUser user = auth.getCurrentUser();
    if (user != null) {
        String userId = user.getUid();

        // Query Firestore to get goals of the current user
        db.collection("goals")
                .whereEqualTo("createdBy", userId) // Filter by user
                .orderBy("createdAt", Query.Direction.ASCENDING) // Optional: Sort by creation date
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null) {
                        Log.e("GoalViewModel", "Error listening for changes", e);
                        return;
                    }

                    if (snapshots != null) {
                        List<Goal> goalList = new ArrayList<>();
                        for (DocumentSnapshot document : snapshots.getDocuments()) {
                            Goal goal = document.toObject(Goal.class);
                            if (goal != null) {
                                goal.setId(document.getId()); // Optionally set the document ID
                                goalList.add(goal);
                            }
                        }
                        goals.setValue(goalList); // Update the LiveData
                        Log.d("GoalViewModel", "Goals updated: " + goalList);
                    }
                });
    }
}

    public void deleteGoal(String goalId) {
        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            // Delete the goal from Firestore
            db.collection("goals")
                    .document(goalId)
                    .delete()
                    .addOnSuccessListener(aVoid -> {
                        Log.d("GoalViewModel", "Goal deleted successfully");

                        // Reload goals after deletion
                        loadGoals();
                    })
                    .addOnFailureListener(e -> Log.e("GoalViewModel", "Error deleting goal", e));
        }
    }
}
