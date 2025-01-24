package ma.ismagi.cp2.transactiontracker.viewModels;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.auth.FirebaseAuth;

public class SettingViewModel extends ViewModel {
    private MutableLiveData<Boolean> isLoggedOut;
    private FirebaseAuth auth;
    public SettingViewModel(){
        isLoggedOut = new MutableLiveData<>(false);
        auth = FirebaseAuth.getInstance();
    }
    public void logout(){
        auth.signOut();
        isLoggedOut.setValue(true);
    }

    public LiveData<Boolean> getIsLoggedOut(){
        return isLoggedOut;
    }
}
