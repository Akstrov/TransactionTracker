package ma.ismagi.cp2.transactiontracker.viewModels;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

import ma.ismagi.cp2.transactiontracker.model.Transaction;

public class RecycleBinViewModel extends ViewModel {
    private final MutableLiveData<List<Transaction>> deletedTransactions = new MutableLiveData<>();
    private final FirebaseFirestore db;

    private FirebaseAuth auth;
    public RecycleBinViewModel() {
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        loadDeletedTransactions();
    }

    public LiveData<List<Transaction>> getDeletedTransactions() {
        return deletedTransactions;
    }

    private void loadDeletedTransactions() {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            String userId = user.getUid();
            db.collection("deletedTransactions")
                    .whereEqualTo("createdBy", userId)
                    .addSnapshotListener((snapshots, e) -> {
                        if (e != null) {
                            Log.e("RecycleBinViewModel", "Error listening for changes", e);
                            return;
                        }

                        if (snapshots != null) {
                            List<Transaction> transactionList = new ArrayList<>();
                            for (DocumentSnapshot document : snapshots.getDocuments()) {
                                Transaction transaction = document.toObject(Transaction.class);
                                if (transaction != null) {
                                    transaction.setId(document.getId());
                                    transactionList.add(transaction);
                                }
                            }
                            deletedTransactions.setValue(transactionList);
                        }
                    });
        }
    }

    // Restore individual transaction
    public void restoreTransaction(Transaction transaction) {
        db.collection("deletedTransactions")
                .document(transaction.getId())
                .delete()
                .addOnSuccessListener(aVoid -> {
                    db.collection("transactions")
                            .document(transaction.getId())
                            .set(transaction)
                            .addOnSuccessListener(aVoid1 -> {
                                Log.d("RecycleBinViewModel", "Transaction restored successfully");
                                updateGoalsForRestoredTransaction(transaction);
                            })
                            .addOnFailureListener(e -> Log.w("RecycleBinViewModel", "Error restoring transaction", e));
                })
                .addOnFailureListener(e -> Log.w("RecycleBinViewModel", "Error deleting transaction from deleted collection", e));
    }

    private void updateGoalsForRestoredTransaction(Transaction transaction) {
        String userId = transaction.getCreatedBy();
        String transactionDate = transaction.getDate();

        // Fetch all goals created by the user
        db.collection("goals")
                .whereEqualTo("createdBy", userId)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    for (DocumentSnapshot goalDoc : querySnapshot.getDocuments()) {
                        String goalId = goalDoc.getId();
                        String goalCreatedAt = goalDoc.getString("createdAt");

                        // Only update goals created before or on the transaction date
                        if (goalCreatedAt != null && goalCreatedAt.compareTo(transactionDate) <= 0) {
                            double targetAmount = goalDoc.getDouble("targetAmount");

                            // Recalculate progress for the goal
                            GoalProgressHelper.fetchAndUpdateCurrentProgress(goalId, targetAmount, goalCreatedAt, goalDoc.getString("goalType"));
                        }
                    }
                })
                .addOnFailureListener(e -> Log.w("RecycleBinViewModel", "Error fetching goals for user", e));
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


    // Restore all deleted transactions
    public void restoreAllTransactions() {
        List<Transaction> transactions = deletedTransactions.getValue();
        if (transactions != null) {
            for (Transaction transaction : transactions) {
                restoreTransaction(transaction);
            }
        }
    }

    // Hard delete Transaction
    public void deleteTransaction(Transaction transaction, Runnable onSuccess) {
        if (transaction != null) {
            db.collection("deletedTransactions")
                    .document(transaction.getId())
                    .delete()
                    .addOnSuccessListener(aVoid -> Log.d("RecycleBinViewModel", "Transaction deleted successfully"))
                    .addOnFailureListener(e -> Log.w("RecycleBinViewModel", "Error deleting transaction", e));
        }
    }

}
