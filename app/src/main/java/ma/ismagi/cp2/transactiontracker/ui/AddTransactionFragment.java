package ma.ismagi.cp2.transactiontracker.ui;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CalendarView;

import com.google.android.material.datepicker.MaterialDatePicker;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import ma.ismagi.cp2.transactiontracker.R;
import ma.ismagi.cp2.transactiontracker.databinding.FragmentAddTransactionBinding;
import ma.ismagi.cp2.transactiontracker.viewModels.AddTransactionViewModel;

public class AddTransactionFragment extends Fragment {

    private FragmentAddTransactionBinding binding;
    private AddTransactionViewModel vm;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentAddTransactionBinding.inflate(inflater, container, false);
        vm = new ViewModelProvider(this).get(AddTransactionViewModel.class);
        binding.setViewModel(vm);
        binding.setLifecycleOwner(this);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                getContext(),
                R.array.transaction_types,
                androidx.appcompat.R.layout.support_simple_spinner_dropdown_item
        );
        adapter.setDropDownViewResource(androidx.appcompat.R.layout.support_simple_spinner_dropdown_item);
        binding.typeSpinner.setAdapter(adapter);
        binding.typeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedType = parent.getItemAtPosition(position).toString();
                vm.getTransaction().getValue().setType(selectedType);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        vm.getTransactionAdded().observe(getViewLifecycleOwner(), transactionAdded -> {
            if (transactionAdded != null && transactionAdded) {
                Navigation.findNavController(binding.getRoot()).navigate(R.id.action_addTransactionFragment_to_transactionsFragment);
            }
        });
        binding.dateEditText.setOnClickListener(v -> {
            showDatePickerDialog();
        });
        return binding.getRoot();
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
            vm.getTransaction().getValue().setDate(formattedDate);
        });
    }
}