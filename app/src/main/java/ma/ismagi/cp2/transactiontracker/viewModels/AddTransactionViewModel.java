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
                    })
                    .addOnFailureListener(e -> {
                        Log.w("AddTransactionViewModel", "Error adding transaction", e);
                        transactionAdded.setValue(false);
                    });
        }
    }
    private void setAmount(String amount){
        transaction.getValue().setAmount(Double.parseDouble(amount));
    }
}
