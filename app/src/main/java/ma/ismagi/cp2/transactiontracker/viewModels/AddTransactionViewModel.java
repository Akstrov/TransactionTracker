package ma.ismagi.cp2.transactiontracker.viewModels;

import android.util.Log;
import android.view.View;

import androidx.databinding.ObservableField;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import ma.ismagi.cp2.transactiontracker.R;
import ma.ismagi.cp2.transactiontracker.model.Transaction;

public class AddTransactionViewModel extends ViewModel {
    private final MutableLiveData<Transaction> transaction = new MutableLiveData<>();
    private final ObservableField<String> type = new ObservableField<>();
    private final MutableLiveData<Boolean> transactionAdded = new MutableLiveData<>();

    private FirebaseAuth auth = FirebaseAuth.getInstance();
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    public AddTransactionViewModel() {
        transaction.setValue(new Transaction());
        type.set("Income");
    }

    public MutableLiveData<Boolean> getTransactionAdded() {
        return transactionAdded;
    }

    public MutableLiveData<Transaction> getTransaction() {
        return transaction;
    }
    public void onSubmitClicked() {
        Log.d("AddTransactionViewModel", "Submitting transaction");
        Transaction newTransaction = transaction.getValue();
        Log.d("AddTransactionViewModel", "Submitting transaction: " + newTransaction);
        if (newTransaction != null) {
            String userId = auth.getCurrentUser().getUid();
            newTransaction.setCreatedBy(userId);
            db.collection("transactions")
                    .add(newTransaction)
                    .addOnSuccessListener(documentReference -> {
                        Log.d("AddTransactionViewModel", "Transaction added with ID: " + documentReference.getId());
                        transactionAdded.setValue(true);
                        // Trigger goal update
                        updateGoalsForUser(userId, newTransaction);
                    })
                    .addOnFailureListener(e -> {
                        Log.w("AddTransactionViewModel", "Error adding transaction", e);
                        transactionAdded.setValue(false);
                    });
        }
    }

    private void updateGoalsForUser(String userId, Transaction newTransaction) {
        db.collection("goals")
                .whereEqualTo("createdBy", userId)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    for (DocumentSnapshot goalDoc : querySnapshot.getDocuments()) {
                        String goalId = goalDoc.getId();
                        String goalCreatedAt = goalDoc.getString("createdAt");

                        // Only update goals created before or on the transaction date
                        if (goalCreatedAt != null && goalCreatedAt.compareTo(newTransaction.getDate()) <= 0) {
                            double targetAmount = goalDoc.getDouble("targetAmount");
//                            fetchAndUpdateCurrentProgress(goalId, targetAmount, goalCreatedAt);
                            GoalProgressHelper.fetchAndUpdateCurrentProgress(goalId, targetAmount, goalCreatedAt, goalDoc.getString("goalType"));
                        }
                    }
                })
                .addOnFailureListener(e -> Log.w("AddTransactionViewModel", "Error fetching goals for user", e));
}
    private void fetchAndUpdateCurrentProgress(String goalId, double targetAmount, String creationDate) {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) return;
        String userId = user.getUid();

        db.collection("transactions")
                .whereEqualTo("createdBy", userId)
                .whereGreaterThanOrEqualTo("date", creationDate)
                .get()
                .addOnSuccessListener(querySnapshot -> {
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

                    double currentProgress = totalIncome - totalExpense;
                    double progressPercentage = (currentProgress / targetAmount) * 100;
                    boolean isCompleted = progressPercentage >= 100;

                    // Update the goal document with the calculated progress
                    db.collection("goals").document(goalId)
                            .update(
                                    "currentProgress", currentProgress,
                                    "progressPercentage", progressPercentage,
                                    "completed", isCompleted
                            )
                            .addOnSuccessListener(unused -> Log.d("AddTransactionViewModel", "Progress updated for goal: " + goalId))
                            .addOnFailureListener(e -> Log.w("AddTransactionViewModel", "Error updating goal progress", e));
                })
                .addOnFailureListener(e -> Log.w("AddTransactionViewModel", "Error fetching transactions for progress update", e));
    }

    private void setAmount(String amount){
        transaction.getValue().setAmount(Double.parseDouble(amount));
    }
}
