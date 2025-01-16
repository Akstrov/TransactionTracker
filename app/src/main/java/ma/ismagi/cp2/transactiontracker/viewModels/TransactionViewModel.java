package ma.ismagi.cp2.transactiontracker.viewModels;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import ma.ismagi.cp2.transactiontracker.model.Transaction;

public class TransactionViewModel extends ViewModel {
    private final MutableLiveData<List<Transaction>> transactions = new MutableLiveData<>();
    private final MutableLiveData<List<Transaction>> deletedTransactions = new MutableLiveData<>();
    private FirebaseAuth auth;
    private FirebaseFirestore db;
    public TransactionViewModel(){
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        loadTransactions("All");
    }
    public LiveData<List<Transaction>> getTransactions() {
        return transactions;
    }
    public LiveData<List<Transaction>> getDeletedTransactions() {
        return deletedTransactions;
    }
    public void loadTransactions(String filterType) {
        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            String userId = user.getUid();

            // Attach a real-time listener to Firestore
            db.collection("transactions")
                    .whereEqualTo("createdBy", userId)
                    .orderBy("date", Query.Direction.DESCENDING)
                    .addSnapshotListener((snapshots, e) -> {
                        if (e != null) {
                            Log.e("TransactionViewModel", "Error listening for changes", e);
                            return;
                        }

                        if (snapshots != null) {
                            ArrayList<Transaction> transactionList = new ArrayList<>();
                            for (DocumentSnapshot document : snapshots.getDocuments()) {
                                Transaction transaction = document.toObject(Transaction.class);
                                if (transaction != null) {
                                    transaction.setId(document.getId());
                                    if (filterType.equals("Income") && !transaction.getType().equals("Income"))
                                        continue;
                                    if (filterType.equals("Expense") && !transaction.getType().equals("Expense"))
                                        continue;
                                    transactionList.add(transaction);
                                }
                            }
                            transactions.setValue(transactionList);
                            Log.d("TransactionViewModel", "Transactions updated: " + transactionList);
                        }
                    });
        } else {
            // Clear the transactions if the user is not logged in
            transactions.setValue(new ArrayList<>());
        }
    }
    // Load deleted transactions
    public void loadDeletedTransactions() {
        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            String userId = user.getUid();
            db.collection("deletedTransactions")
                    .whereEqualTo("createdBy", userId)
                    .orderBy("date", Query.Direction.DESCENDING)
                    .addSnapshotListener((snapshots, e) -> {
                        if (e != null) {
                            Log.e("TransactionViewModel", "Error listening for deleted transactions", e);
                            return;
                        }
                        if (snapshots != null) {
                            ArrayList<Transaction> deletedList = new ArrayList<>();
                            for (DocumentSnapshot document : snapshots.getDocuments()) {
                                Transaction transaction = document.toObject(Transaction.class);
                                if (transaction != null) {
                                    transaction.setId(document.getId());
                                    deletedList.add(transaction);
                                }
                            }
                            deletedTransactions.setValue(deletedList);
                            Log.d("TransactionViewModel", "Deleted transactions updated: " + deletedList);
                        }
                    });
        } else {
            deletedTransactions.setValue(new ArrayList<>());
        }
    }

    public void deleteTransaction(Transaction transaction, Runnable onSuccess){
        Log.d("TransactionViewModel", "Deleting transaction: " + transaction);
        String documentId = transaction.getId();
        if (documentId != null) {
//            db.collection("transactions").document(documentId)
//                    .delete()
//                    .addOnSuccessListener(aVoid -> {
//                        Log.d("TransactionViewModel", "Transaction deleted successfully");
//                        onSuccess.run();
//                    })
//                    .addOnFailureListener(e -> Log.w("TransactionViewModel", "Error deleting transaction", e));
            db.collection("deletedTransactions")
                    .document(documentId) // Use the same ID to maintain traceability
                    .set(transaction)
                    .addOnSuccessListener(aVoid -> {
                        Log.d("DeleteTransaction", "Transaction moved to deletedTransactions");

                        // Step 2: Remove the transaction from the "transactions" collection
                        db.collection("transactions")
                                .document(transaction.getId())
                                .delete()
                                .addOnSuccessListener(aVoid1 -> Log.d("DeleteTransaction", "Transaction deleted from transactions collection"))
                                .addOnFailureListener(e -> Log.e("DeleteTransaction", "Failed to delete transaction from transactions collection", e));
                    })
                    .addOnFailureListener(e -> Log.e("DeleteTransaction", "Failed to move transaction to deletedTransactions", e));

        }
    }
}
