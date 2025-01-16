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
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

import ma.ismagi.cp2.transactiontracker.R;
import ma.ismagi.cp2.transactiontracker.adapter.TransactionAdapter;
import ma.ismagi.cp2.transactiontracker.databinding.FragmentRecycleBinBinding;
import ma.ismagi.cp2.transactiontracker.model.Transaction;
import ma.ismagi.cp2.transactiontracker.viewModels.RecycleBinViewModel;

public class RecycleBinFragment extends Fragment {
    private FragmentRecycleBinBinding binding;
    private RecycleBinViewModel viewModel;
    private TransactionAdapter adapter;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentRecycleBinBinding.inflate(getLayoutInflater());
        viewModel = new ViewModelProvider(this).get(RecycleBinViewModel.class);
        binding.setViewModel(viewModel);
        binding.setLifecycleOwner(this);

        adapter = new TransactionAdapter(new ArrayList<>());
        binding.setAdapter(adapter);

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel.getDeletedTransactions().observe(getViewLifecycleOwner(), transactions -> {
            if (transactions != null) {
                adapter.updateTransactions(transactions);
            }
        });
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {

            private final ColorDrawable restoreBackground = new ColorDrawable(Color.parseColor("#4CAF50")); // Green for restore
            private final ColorDrawable deleteBackground = new ColorDrawable(Color.RED); // Red for delete
            private final Drawable restoreIcon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_restore); // Restore icon
            private final Drawable deleteIcon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_delete); // Delete icon

            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false; // Drag functionality not implemented
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                Transaction transaction = binding.getAdapter().getTransactionAt(position);

                if (direction == ItemTouchHelper.LEFT) {
                    // Handle delete action
                    viewModel.deleteTransaction(transaction, () -> {
                        Log.d("SwipeAction", "Transaction moved to deletedTransactions");
                    });
                } else if (direction == ItemTouchHelper.RIGHT) {
                    // Handle restore action
                    viewModel.restoreTransaction(transaction);
                    Log.d("SwipeAction", "Transaction restored");
                }
            }

            @Override
            public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
                View itemView = viewHolder.itemView;
                Paint paint = new Paint();

                // For swiping left (delete)
                if (dX < 0) {
                    deleteBackground.setBounds(itemView.getRight() + (int) dX, itemView.getTop(), itemView.getRight(), itemView.getBottom());
                    deleteBackground.draw(c);

                    if (deleteIcon != null) {
                        int iconSize = itemView.getHeight() / 2;
                        int iconMargin = (itemView.getHeight() - iconSize) / 2;
                        int iconTop = itemView.getTop() + iconMargin;
                        int iconBottom = iconTop + iconSize;
                        int iconLeft = itemView.getRight() - iconMargin - iconSize;
                        int iconRight = iconLeft + iconSize;

                        deleteIcon.setBounds(iconLeft, iconTop, iconRight, iconBottom);
                        deleteIcon.draw(c);
                    }
                }
                // For swiping right (restore)
                else if (dX > 0) {
                    restoreBackground.setBounds(itemView.getLeft(), itemView.getTop(), (int) (itemView.getLeft() + dX), itemView.getBottom());
                    restoreBackground.draw(c);

                    if (deleteIcon != null) {
                        int iconSize = itemView.getHeight() / 2;
                        int iconMargin = (itemView.getHeight() - iconSize) / 2;
                        int iconTop = itemView.getTop() + iconMargin;
                        int iconBottom = iconTop + iconSize;
                        int iconLeft = itemView.getLeft() + iconMargin;
                        int iconRight = iconLeft + iconSize;

                        restoreIcon.setBounds(iconLeft, iconTop, iconRight, iconBottom);
                        restoreIcon.draw(c);
                    }
                }

                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            }
        });
        itemTouchHelper.attachToRecyclerView(binding.recyclerViewDeletedTransactions);
    }
}