package ma.ismagi.cp2.transactiontracker.ui;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.snackbar.Snackbar;

import ma.ismagi.cp2.transactiontracker.R;
import ma.ismagi.cp2.transactiontracker.databinding.FragmentSignInBinding;
import ma.ismagi.cp2.transactiontracker.viewModels.UserViewModel;

public class SignInFragment extends Fragment {

    private FragmentSignInBinding binding;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentSignInBinding.inflate(inflater, container, false);
        UserViewModel vm = new ViewModelProvider(this).get(UserViewModel.class);
        binding.setViewmodel(vm);
        vm.getStatus().observe(getViewLifecycleOwner(), status -> {
            switch (status){
                case SUCCESS:
                    binding.progressBar.setVisibility(View.GONE);
                    NavHostFragment.findNavController(this).navigate(R.id.action_signInFragment_to_homeFragment);
                    SharedPreferences sharedPreferences = getActivity().getSharedPreferences("user", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("userNamer", vm.getUser().getValue().getFirstName()+" "+vm.getUser().getValue().getLastName());
                    editor.apply();
                    break;
                case ERROR:
                    binding.progressBar.setVisibility(View.GONE);
                    vm.getErrorMessage().observe(getViewLifecycleOwner(),errorMessage -> {
                        Snackbar.make(getContext(), binding.getRoot(), errorMessage, Snackbar.LENGTH_LONG).show();
                    });
                    break;
                case LOADING:
                    binding.progressBar.setVisibility(View.VISIBLE);
                    break;
            }
        });
        binding.btnSignUp.setOnClickListener( v -> {
            NavHostFragment.findNavController(SignInFragment.this).
                    navigate(R.id.action_signInFragment_to_signUpFragment);
        });
        return binding.getRoot();
    }
}