package ma.ismagi.cp2.transactiontracker.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import ma.ismagi.cp2.transactiontracker.R;
import ma.ismagi.cp2.transactiontracker.databinding.ItemGoalBinding;
import ma.ismagi.cp2.transactiontracker.databinding.ItemTransactionBinding;
import ma.ismagi.cp2.transactiontracker.model.Goal;

public class GoalAdapter  extends RecyclerView.Adapter<GoalAdapter.GoalViewHolder> {
    private List<Goal> goals;

    public GoalAdapter(List<Goal> goals){
        this.goals = goals;
    }

    @NonNull
    @Override
    public GoalViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemGoalBinding binding = ItemGoalBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new GoalViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull GoalViewHolder holder, int position) {
        Goal goal = goals.get(position);
        holder.bind(goal);
    }

    @Override
    public int getItemCount() {
        return goals.size();
    }

    // ViewHolder class
    static class GoalViewHolder extends RecyclerView.ViewHolder {
        private final ItemGoalBinding binding;
        public GoalViewHolder(ItemGoalBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
        public void bind(Goal goal) {
            binding.setGoal(goal);
            binding.executePendingBindings();
        }
    }
    public void updateGoals(List<Goal> newGoals) {
        this.goals = newGoals;
        notifyDataSetChanged();
    }

    public List<Goal> getGoals() {
        return goals;
    }
    public void removeGoal(int position) {
        goals.remove(position);
        notifyItemRemoved(position);
    }

}
