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

    public RecycleBinViewModel() {
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
                            })
                            .addOnFailureListener(e -> Log.w("RecycleBinViewModel", "Error restoring transaction", e));
                })
                .addOnFailureListener(e -> Log.w("RecycleBinViewModel", "Error deleting transaction from deleted collection", e));
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
