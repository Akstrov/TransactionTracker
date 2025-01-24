package ma.ismagi.cp2.transactiontracker.ui;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import ma.ismagi.cp2.transactiontracker.R;
import ma.ismagi.cp2.transactiontracker.adapter.GoalAdapter;
import ma.ismagi.cp2.transactiontracker.databinding.FragmentGoalsBinding;
import ma.ismagi.cp2.transactiontracker.model.Goal;
import ma.ismagi.cp2.transactiontracker.viewModels.GoalViewModel;

public class GoalsFragment extends Fragment {
    private FragmentGoalsBinding binding;
    private GoalViewModel viewModel;
    private GoalAdapter adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentGoalsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(GoalViewModel.class);
        viewModel.getGoals().observe(getViewLifecycleOwner(), goals -> {
            Log.d("GoalsFragment", "Received goals: " + goals);
            adapter = new GoalAdapter(goals);
            binding.setAdapter(adapter);
            // Attach ItemTouchHelper for swipe gestures
            ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {

                private final ColorDrawable deleteBackground = new ColorDrawable(Color.RED);
                private final ColorDrawable editBackground = new ColorDrawable(Color.GREEN);
                private final Drawable deleteIcon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_delete);
                private final Drawable editIcon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_edit);

                @Override
                public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                    return false; // Drag functionality not implemented
                }

                @Override
                public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                    int position = viewHolder.getAdapterPosition();
                    Goal goal = adapter.getGoals().get(position);

                    if (direction == ItemTouchHelper.LEFT) {
                        // Delete the goal (swipe left)
                        viewModel.deleteGoal(goal.getId());
                    } else if (direction == ItemTouchHelper.RIGHT) {
                        // Update the goal (swipe right)
                        updateGoal(goal);
                    }
                }

                @Override
                public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
                    View itemView = viewHolder.itemView;
                    Paint paint = new Paint();

                    // Set background color for delete (swipe left)
                    if (dX < 0) {
                        paint.setColor(Color.RED);
                        c.drawRect((float) itemView.getRight() + dX, (float) itemView.getTop(),
                                (float) itemView.getRight(), (float) itemView.getBottom(), paint);

                        // Add delete icon for visual feedback
                        if (deleteIcon != null) {
                            int iconSize = itemView.getHeight() / 2;
                            int iconMargin = (itemView.getHeight() - iconSize) / 2;

                            int iconTop = itemView.getTop() + iconMargin;
                            int iconBottom = iconTop + iconSize;
                            int iconLeft = itemView.getRight() - iconMargin - iconSize;
                            int iconRight = itemView.getRight() - iconMargin;

                            deleteIcon.setBounds(iconLeft, iconTop, iconRight, iconBottom);
                            deleteIcon.draw(c);
                        }
                    }
                    // Set background color for update (swipe right)
                    else if (dX > 0) {
                        paint.setColor(Color.GREEN);
                        c.drawRect((float) itemView.getLeft(), (float) itemView.getTop(),
                                (float) itemView.getLeft() + dX, (float) itemView.getBottom(), paint);

                        // Add edit icon for visual feedback
                        if (editIcon != null) {
                            int iconSize = itemView.getHeight() / 2;
                            int iconMargin = (itemView.getHeight() - iconSize) / 2;

                            int iconTop = itemView.getTop() + iconMargin;
                            int iconBottom = iconTop + iconSize;
                            int iconLeft = itemView.getLeft() + iconMargin;
                            int iconRight = iconLeft + iconSize;

                            editIcon.setBounds(iconLeft, iconTop, iconRight, iconBottom);
                            editIcon.draw(c);
                        }
                    }

                    super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
                }
            });

            itemTouchHelper.attachToRecyclerView(binding.recyclerViewGoals);
        });
        binding.fabAddGoal.setOnClickListener(v -> {
            Navigation.findNavController(v).navigate(R.id.action_goalsFragment_to_addGoalFragment);
        });
    }

    private void updateGoal(Goal goal) {
        // Navigate to edit goal fragment
        Bundle bundle = new Bundle();
        bundle.putSerializable("goal", goal);
        Navigation.findNavController(getView()).navigate(R.id.action_goalsFragment_to_editGoalFragment, bundle);
    }

}