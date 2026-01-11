package com.classbuddy.app.ui.student.notification;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.classbuddy.app.R;
import com.classbuddy.app.adapter.NotificationAdapter;
import com.classbuddy.app.data.model.Notification;
import com.classbuddy.app.databinding.FragmentNotificationCenterBinding;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;

public class NotificationCenterFragment extends Fragment {

    private FragmentNotificationCenterBinding binding;
    private NotificationCenterViewModel viewModel;
    private NotificationAdapter adapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentNotificationCenterBinding. inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(NotificationCenterViewModel.class);

        setupRecyclerView();
        setupClickListeners();
        setupSwipeToDelete();
        observeViewModel();
    }

    private void setupRecyclerView() {
        adapter = new NotificationAdapter((notification, position) -> {
            // Mark as read and handle click
            viewModel.markAsRead(notification.getId());
            handleNotificationClick(notification);
        });

        binding.rvNotifications. setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvNotifications.setAdapter(adapter);
    }

    private void setupClickListeners() {
        binding.toolbar.setOnMenuItemClickListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.action_mark_all_read) {
                viewModel.markAllAsRead();
                Toast.makeText(requireContext(), "All marked as read", Toast.LENGTH_SHORT).show();
                return true;
            } else if (itemId == R. id.action_clear_all) {
                showClearAllDialog();
                return true;
            }
            return false;
        });

        binding.swipeRefresh.setOnRefreshListener(() -> {
            viewModel.refreshNotifications();
        });
    }

    private void setupSwipeToDelete() {
        ItemTouchHelper. SimpleCallback swipeCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView,
                                  @NonNull RecyclerView. ViewHolder viewHolder,
                                  @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                Notification notification = adapter.getCurrentList().get(position);

                viewModel.deleteNotification(notification. getId());

                Snackbar.make(binding. getRoot(), "Notification deleted", Snackbar.LENGTH_LONG)
                        .setAction("Undo", v -> {
                            // Undo delete - reload notifications
                            viewModel.refreshNotifications();
                        })
                        .show();
            }
        };

        new ItemTouchHelper(swipeCallback).attachToRecyclerView(binding.rvNotifications);
    }

    private void handleNotificationClick(Notification notification) {
        // Navigate based on notification type
        String type = notification.getType();
        String referenceId = notification.getReferenceId();

        if (referenceId == null || referenceId.isEmpty()) {
            showNotificationDetail(notification);
            return;
        }

        Bundle bundle = new Bundle();

        switch (type) {
            case com.classbuddy.app.util.Constants.NOTIFICATION_TYPE_EXAM:
                // Navigate to exam detail
                showNotificationDetail(notification);
                break;
            case com.classbuddy.app.util.Constants. NOTIFICATION_TYPE_NOTICE:
                // Navigate to notice
                showNotificationDetail(notification);
                break;
            case com.classbuddy.app.util.Constants.NOTIFICATION_TYPE_CLASSROOM:
                bundle.putString("classroomId", referenceId);
                // Navigate to classroom
                break;
            default:
                showNotificationDetail(notification);
        }
    }

    private void showNotificationDetail(Notification notification) {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(notification.getTitle())
                .setMessage(notification.getMessage())
                .setPositiveButton(R.string.ok, null)
                .show();
    }

    private void showClearAllDialog() {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.clear_all)
                .setMessage("Are you sure you want to clear all notifications? ")
                .setPositiveButton(R.string.yes, (dialog, which) -> {
                    viewModel.clearAllNotifications();
                    Toast.makeText(requireContext(), "All notifications cleared", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton(R.string.no, null)
                .show();
    }

    private void observeViewModel() {
        viewModel.getNotifications().observe(getViewLifecycleOwner(), resource -> {
            binding.swipeRefresh.setRefreshing(false);

            switch (resource.status) {
                case LOADING:
                    binding.progressBar.setVisibility(View. VISIBLE);
                    binding.layoutEmpty.setVisibility(View. GONE);
                    break;

                case SUCCESS:
                    binding.progressBar.setVisibility(View.GONE);
                    if (resource.data != null && ! resource.data.isEmpty()) {
                        binding. rvNotifications.setVisibility(View.VISIBLE);
                        binding.layoutEmpty.setVisibility(View.GONE);
                        adapter.submitList(resource.data);

                        // Update unread count in toolbar
                        long unreadCount = resource.data. stream()
                                .filter(n -> ! n.isRead())
                                .count();
                        if (unreadCount > 0) {
                            binding.toolbar. setSubtitle(unreadCount + " unread");
                        } else {
                            binding.toolbar.setSubtitle(null);
                        }
                    } else {
                        binding.rvNotifications. setVisibility(View.GONE);
                        binding.layoutEmpty.setVisibility(View. VISIBLE);
                        binding.toolbar.setSubtitle(null);
                    }
                    break;

                case ERROR:
                    binding.progressBar.setVisibility(View. GONE);
                    binding.layoutEmpty.setVisibility(View. VISIBLE);
                    break;
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
