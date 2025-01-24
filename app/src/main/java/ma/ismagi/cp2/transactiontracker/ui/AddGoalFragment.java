package ma.ismagi.cp2.transactiontracker.ui;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import com.google.android.material.datepicker.MaterialDatePicker;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import ma.ismagi.cp2.transactiontracker.R;
import ma.ismagi.cp2.transactiontracker.databinding.FragmentAddGoalBinding;
import ma.ismagi.cp2.transactiontracker.viewModels.AddGoalViewModel;
import ma.ismagi.cp2.transactiontracker.viewModels.GoalViewModel;

public class AddGoalFragment extends Fragment {
    private FragmentAddGoalBinding binding;
    private AddGoalViewModel viewModel;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentAddGoalBinding.inflate(inflater, container, false);
        viewModel = new ViewModelProvider(this).get(AddGoalViewModel.class);
        binding.setViewModel(viewModel);
        binding.setLifecycleOwner(this);
        setupAutoComplete();
        setupAddButton();
        binding.dateEditText.setOnClickListener(v -> {
            showDatePickerDialog();
        });
        viewModel.getGoalAdded().observe(getViewLifecycleOwner(), goalAdded -> {
            if (goalAdded != null && goalAdded) {
                Navigation.findNavController(binding.getRoot()).navigate(R.id.action_addGoalFragment_to_goalsFragment);
            }
        });
        return binding.getRoot();
    }

    private void setupAddButton() {
    }

    private void setupAutoComplete() {
        String[] goalTypes = getResources().getStringArray(R.array.goal_types);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, goalTypes);
        binding.autoCompleteGoalType.setAdapter(adapter);
        // select savings by default
        binding.autoCompleteGoalType.setText(goalTypes[0], false);
    }
    public void showDatePickerDialog() {
        // Create a new instance of the date picker dialog
        MaterialDatePicker<Long> datePicker = MaterialDatePicker.Builder.datePicker()
                .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                .build();
        // show the dialog
        datePicker.show(getParentFragmentManager(), datePicker.toString());
        // handle date selection
        datePicker.addOnPositiveButtonClickListener(selection -> {
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(selection);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            String formattedDate = sdf.format(calendar.getTime());
            binding.dateEditText.setText(formattedDate);
//            vm.getTransaction().getValue().setDate(formattedDate);
        });
    }

}