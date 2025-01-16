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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import java.util.ArrayList;
import java.util.List;

import ma.ismagi.cp2.transactiontracker.R;
import ma.ismagi.cp2.transactiontracker.adapter.TransactionAdapter;
import ma.ismagi.cp2.transactiontracker.databinding.FragmentTransactionsBinding;
import ma.ismagi.cp2.transactiontracker.model.Transaction;
import ma.ismagi.cp2.transactiontracker.viewModels.TransactionViewModel;

public class TransactionsFragment extends Fragment {
    private FragmentTransactionsBinding binding;
    private TransactionViewModel viewModel;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentTransactionsBinding.inflate(inflater, container, false);
        binding.fabAddTransaction.setOnClickListener(v -> {
            Navigation.findNavController(v).navigate(R.id.action_transactionsFragment_to_addTransactionFragment);
        });
        binding.fabRecycleBin.setOnClickListener(v -> {
            Navigation.findNavController(v).navigate(R.id.action_transactionsFragment_to_recycleBinFragment);
        });
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(TransactionViewModel.class);
        // set up autocomplete filter
        String[] filterOptions = getResources().getStringArray(R.array.transaction_filter_options);
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, filterOptions);
        binding.autocompleteFilter.setAdapter(arrayAdapter);
        binding.autocompleteFilter.setText(filterOptions[0], false);

        // Set up spinner for transaction filtering
        binding.autocompleteFilter.setOnItemClickListener((parent, view1, position, id) -> {
            String selectedFilter = parent.getItemAtPosition(position).toString();
            viewModel.loadTransactions(selectedFilter);
        });
        viewModel.getTransactions().observe(getViewLifecycleOwner(), transactions -> {
            Log.d("TransactionsFragment", "Received transactions: " + transactions);
            TransactionAdapter adapter = new TransactionAdapter(transactions);
            binding.setAdapter(adapter);

            // Attach ItemTouchHelper here
            ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {

                private final ColorDrawable deleteBackground = new ColorDrawable(Color.RED);
                private final ColorDrawable editBackground = new ColorDrawable(Color.BLUE);
                private final Drawable deleteIcon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_delete);
                private final Drawable editIcon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_edit);

                @Override
                public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                    return false; // Drag functionality not implemented
                }

                @Override
                public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                    int position = viewHolder.getAdapterPosition();
                    Transaction transaction = binding.getAdapter().getTransactionAt(position);

                    if (direction == ItemTouchHelper.LEFT) {
                        viewModel.deleteTransaction(transaction, () -> {
//                            binding.getAdapter().removeTransaction(position);
                            Log.d("SwipeAction", "Transaction moved to deletedTransactions");

                        });
                    } else if (direction == ItemTouchHelper.RIGHT) {
                        updateTransaction(transaction);
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

                        // Add an icon for visual feedback (optional)
                        Drawable deleteIcon = ContextCompat.getDrawable(recyclerView.getContext(), R.drawable.ic_delete);
                        if (deleteIcon != null) {
                            int iconSize = itemView.getHeight() / 2;  // Scale icon to half the height of the item view
                            int iconMargin = (itemView.getHeight() - iconSize) / 2; // Center icon vertically

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

                        // Add an icon for visual feedback (optional)
                        Drawable editIcon = ContextCompat.getDrawable(recyclerView.getContext(), R.drawable.ic_edit);
                        if (editIcon != null) {
                            int iconSize = itemView.getHeight() / 2;  // Scale icon to half the height of the item view
                            int iconMargin = (itemView.getHeight() - iconSize) / 2; // Center icon vertically

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
            itemTouchHelper.attachToRecyclerView(binding.recyclerViewTransactions);
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        String[] filterOptions = getResources().getStringArray(R.array.transaction_filter_options);
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, filterOptions);
        binding.autocompleteFilter.setAdapter(arrayAdapter);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
    private void updateTransaction(Transaction transaction) {
        Bundle bundle = new Bundle();
        bundle.putSerializable("transaction", transaction);
        Navigation.findNavController(getView()).navigate(R.id.action_transactionsFragment_to_editTransactionFragment, bundle);
    }
}