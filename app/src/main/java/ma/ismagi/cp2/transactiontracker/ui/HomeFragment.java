package ma.ismagi.cp2.transactiontracker.ui;

import android.os.Bundle;

import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;

import ma.ismagi.cp2.transactiontracker.R;
import ma.ismagi.cp2.transactiontracker.adapter.GoalAdapter;
import ma.ismagi.cp2.transactiontracker.databinding.FragmentHomeBinding;
import ma.ismagi.cp2.transactiontracker.viewModels.HomeViewModel;

public class HomeFragment extends Fragment {
    private HomeViewModel homeViewModel;
    private FragmentHomeBinding binding;
    private GoalAdapter goalAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout and get the binding instance
        binding = FragmentHomeBinding.inflate(inflater, container, false);

        // Initialize ViewModel
        homeViewModel = new ViewModelProvider(this).get(HomeViewModel.class);

        // Set the ViewModel to binding
        binding.setViewModel(homeViewModel);
        binding.setLifecycleOwner(getViewLifecycleOwner());

        binding.goalsRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.goalsRecyclerView.setAdapter(goalAdapter);


        // Observe summary data and goals list
        homeViewModel.getSummaryData().observe(getViewLifecycleOwner(), summary -> {
            if (summary != null) {
                binding.summaryIncome.setText(String.format("Income: $%.2f", summary.getTotalIncome()));
                binding.summaryExpense.setText(String.format("Expenses: $%.2f", summary.getTotalExpense()));
            }
        });

        homeViewModel.getGoalsList().observe(getViewLifecycleOwner(), goals -> {
            Log.d("HomeFragment", "Goals count: " + (goals != null ? goals.size() : 0)); // Log total goals
            goalAdapter.updateGoals(goals); // Update the goals list in the adapter
        });

        // Initialize GoalAdapter with empty list
        goalAdapter = new GoalAdapter(new ArrayList<>());
        binding.goalsRecyclerView.setAdapter(goalAdapter);

        // Fetch data for goals and summary
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        homeViewModel.fetchGoals(userId);
        homeViewModel.fetchSummary(userId);

        return binding.getRoot();
    }
}
