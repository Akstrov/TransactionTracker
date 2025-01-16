package ma.ismagi.cp2.transactiontracker.viewModels;

import android.content.SharedPreferences;
import android.util.Log;

import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Source;
import com.google.firebase.firestore.core.FirestoreClient;

import ma.ismagi.cp2.transactiontracker.model.Status;
import ma.ismagi.cp2.transactiontracker.model.User;

public class UserViewModel extends ViewModel {
    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private MutableLiveData<User> user = new MediatorLiveData<>();
    private MutableLiveData<Status> status = new MutableLiveData<>();
    private MutableLiveData<String> errorMessage = new MutableLiveData<>();
    public UserViewModel() {
        user.setValue(new User());
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        errorMessage.setValue("");
    }

    public MutableLiveData<User> getUser() {
        return user;
    }

    public void setUser(MutableLiveData<User> user) {
        this.user = user;
    }

    public MutableLiveData<Status> getStatus() {
        return status;
    }
    public MutableLiveData<String> getErrorMessage() {
        return errorMessage;
    }
    public void signIn(){
        status.setValue(Status.LOADING);
        String email = user.getValue().getEmail();
        String password = user.getValue().getPassword();
        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
            if(task.isSuccessful()){
                FirebaseUser firebaseUser = auth.getCurrentUser();
                DocumentReference documentReference = db.collection("users").document(firebaseUser.getUid());
                Log.d("TAGSIGNIN","Successful SignIn: "+firebaseUser.getUid());
                documentReference.get().addOnCompleteListener(task1 -> {
                    if (task1.isSuccessful()){
                        DocumentSnapshot document = task1.getResult();
                        User user = document.toObject(User.class);
                        Log.d("TAGSIGNIN","DocumentSnapshot data: "+user.toString());
                    }else{
                        Log.d("TAGSIGNIN","No DocumentSnapshot data");
                    }
                });
                status.setValue(Status.SUCCESS);
            }else{
                errorMessage.setValue("Email ou mot de passe incorrect.");
                status.setValue(Status.ERROR);
            }
        });
    }
    public void signUp(){
        status.setValue(Status.LOADING);
        Log.d("TAGSIGNUP", "signUp: "+ user.getValue().toString());
        String email = user.getValue().getEmail();
        String password = user.getValue().getPassword();
        auth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(task -> {
            if(task.isSuccessful()){
                FirebaseUser firebaseUser = auth.getCurrentUser();
                User user = this.user.getValue();
                db.collection("users").document(firebaseUser.getUid()).set(user);
                Log.d("TAGSIGNUP","Successful SignUp: "+firebaseUser.getUid());
                status.setValue(Status.SUCCESS);
            }else{
                try {
                    throw task.getException();
                }
                catch (FirebaseAuthUserCollisionException e) {
                    errorMessage.setValue("Email exist deja.");
                }
                catch (FirebaseAuthWeakPasswordException e) {
                    errorMessage.setValue("Mot de passe faible.");
                }
                catch (FirebaseAuthInvalidCredentialsException e) {
                    errorMessage.setValue("Email invalide.");
                }
                catch(FirebaseException e) {
                    errorMessage.setValue("Mot de passe faible.");
                }
                catch (Exception e) {
                    errorMessage.setValue("Erreur d'insctiption");
                }
                Log.d("TAGSIGNUP","SignUp error: "+task.getException().getMessage());
                status.setValue(Status.ERROR);
            }
        });
    }
}
