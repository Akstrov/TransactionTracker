package ma.ismagi.cp2.transactiontracker.viewModels;

import android.util.Log;
import android.view.View;

import androidx.databinding.ObservableField;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import ma.ismagi.cp2.transactiontracker.model.Transaction;

public class EditTransactionViewModel extends ViewModel {
    private final MutableLiveData<Transaction> transaction = new MutableLiveData<>();
    private final ObservableField<String> type = new ObservableField<>();
    private final MutableLiveData<Boolean> transactionUpdated = new MutableLiveData<>();
    private FirebaseAuth auth = FirebaseAuth.getInstance();
    private FirebaseFirestore db = FirebaseFirestore.getInstance();


    public EditTransactionViewModel() {
        transaction.setValue(new Transaction());
        type.set(transaction.getValue().getType());
    }

    public MutableLiveData<Boolean> getTransactionUpdated() {
        return transactionUpdated;
    }

    public LiveData<Transaction> getTransaction() {
        return transaction;
    }

    public void setTransaction(Transaction transaction) {
        this.transaction.setValue(transaction);
    }

    public void onSaveClicked() {
        Log.d("EditTransactionViewModel", "onSaveClicked called: "+transaction.getValue());
        db.collection("transactions")
                .document(transaction.getValue().getId())
                .set(transaction.getValue())
                .addOnSuccessListener(v -> {
                    Log.d("EditTransactionViewModel", "Transaction updated successfully");
                    transactionUpdated.setValue(true);
                })
                .addOnFailureListener(e -> {
                    Log.w("EditTransactionViewModel", "Error updating transaction", e);
                    transactionUpdated.setValue(false);
                });
    }
    private void setAmount(String amount){
        transaction.getValue().setAmount(Double.parseDouble(amount));
    }
}
