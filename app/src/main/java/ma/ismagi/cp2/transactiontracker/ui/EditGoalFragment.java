package ma.ismagi.cp2.transactiontracker.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.google.android.material.datepicker.MaterialDatePicker;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import ma.ismagi.cp2.transactiontracker.R;
import ma.ismagi.cp2.transactiontracker.databinding.FragmentEditGoalBinding;
import ma.ismagi.cp2.transactiontracker.model.Goal;
import ma.ismagi.cp2.transactiontracker.viewModels.EditGoalViewModel;

public class EditGoalFragment extends Fragment {

    private FragmentEditGoalBinding binding;
    private EditGoalViewModel vm;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentEditGoalBinding.inflate(inflater, container, false);
        if (getArguments() != null) {
            Goal goal = (Goal) getArguments().getSerializable("goal");
            vm = new ViewModelProvider(this).get(EditGoalViewModel.class);
            vm.setGoal(goal);
        }
        binding.setViewModel(vm);
        binding.setLifecycleOwner(this);

        // Set up AutoCompleteTextView for goal type
        setupAutoComplete();

        // Handle date picker click
        binding.deadlineEditText.setText(vm.getGoal().getValue().getDeadline());
        binding.deadlineEditText.setOnClickListener(v -> showDatePickerDialog());

        // Observe the goal updated LiveData
        vm.getGoalUpdated().observe(getViewLifecycleOwner(), goalUpdated -> {
            if (goalUpdated != null && goalUpdated) {
                Navigation.findNavController(binding.getRoot()).navigate(R.id.action_editGoalFragment_to_goalsFragment);
            } else {
                Toast.makeText(getContext(), "Error updating goal", Toast.LENGTH_SHORT).show();
            }
        });

        return binding.getRoot();
    }

    private void setupAutoComplete() {
        String[] goalTypes = getResources().getStringArray(R.array.goal_types);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, goalTypes);
        binding.autoCompleteGoalType.setAdapter(adapter);

        // Set the current goal type as the default value
        String currentGoalType = vm.getGoal().getValue().getGoalType();
        binding.autoCompleteGoalType.setText(currentGoalType, false);

        // Handle selection of a new goal type
        binding.autoCompleteGoalType.setOnItemClickListener((parent, view, position, id) -> {
            String selectedType = parent.getItemAtPosition(position).toString();
            vm.getGoal().getValue().setGoalType(selectedType);
        });
    }

    public void showDatePickerDialog() {
        // Create a new instance of the date picker dialog
        MaterialDatePicker<Long> datePicker = MaterialDatePicker.Builder.datePicker()
                .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                .build();

        // Show the dialog
        datePicker.show(getParentFragmentManager(), datePicker.toString());

        // Handle date selection
        datePicker.addOnPositiveButtonClickListener(selection -> {
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(selection);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            String formattedDate = sdf.format(calendar.getTime());
            binding.deadlineEditText.setText(formattedDate);
            vm.getGoal().getValue().setDeadline(formattedDate);
        });
    }
}
