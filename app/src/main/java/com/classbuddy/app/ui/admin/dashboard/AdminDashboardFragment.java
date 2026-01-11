package com.classbuddy.app.ui.admin.dashboard;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.bumptech.glide.Glide;
import com.classbuddy.app.R;
import com.classbuddy.app.databinding.FragmentAdminDashboardBinding;
import com.classbuddy.app.ui.auth.LoginActivity;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class AdminDashboardFragment extends Fragment {

    private FragmentAdminDashboardBinding binding;
    private AdminDashboardViewModel viewModel;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentAdminDashboardBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super. onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(AdminDashboardViewModel.class);

        setupClickListeners();
        observeViewModel();
    }

    private void setupClickListeners() {
        binding. cardCreateClassroom.setOnClickListener(v -> {
            Navigation.findNavController(v).navigate(R.id.action_dashboard_to_createClassroom);
        });

        binding.cardManageExams.setOnClickListener(v -> {
            Navigation.findNavController(v).navigate(R.id.adminExamFragment);
        });

        binding.cardPostNotice.setOnClickListener(v -> {
            Navigation.findNavController(v).navigate(R.id. adminNoticeFragment);
        });

        binding.cardViewStudents.setOnClickListener(v -> {
            Navigation. findNavController(v).navigate(R.id.studentManagementFragment);
        });

        binding.btnLogout.setOnClickListener(v -> showLogoutDialog());

        binding.swipeRefresh.setOnRefreshListener(() -> viewModel.refreshData());
    }

    private void observeViewModel() {
        // User data
        viewModel.getCurrentUser().observe(getViewLifecycleOwner(), resource -> {
            if (resource. isSuccess() && resource.data != null) {
                binding.tvAdminName.setText(resource. data.getFullName());
                binding.tvAdminEmail.setText(resource.data.getEmail());

                if (resource.data. getProfileImageUrl() != null) {
                    Glide.with(this)
                            . load(resource.data.getProfileImageUrl())
                            . placeholder(R.drawable. ic_profile)
                            .circleCrop()
                            .into(binding.ivProfile);
                }
            }
        });

        // Stats
        viewModel. getTotalClassrooms().observe(getViewLifecycleOwner(), count -> {
            binding.tvClassroomCount.setText(String.valueOf(count));
        });

        viewModel. getTotalStudents().observe(getViewLifecycleOwner(), count -> {
            binding. tvStudentCount. setText(String.valueOf(count));
        });

        viewModel.getUpcomingEventsCount().observe(getViewLifecycleOwner(), count -> {
            binding. tvEventCount.setText(String.valueOf(count));
        });

        viewModel.getActiveNoticesCount().observe(getViewLifecycleOwner(), count -> {
            binding. tvNoticeCount. setText(String.valueOf(count));
        });

        viewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            binding.swipeRefresh.setRefreshing(isLoading);
        });
    }

    private void showLogoutDialog() {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Logout")
                .setMessage(R.string.logout_confirm)
                .setPositiveButton(R.string.yes, (dialog, which) -> {
                    viewModel.logout();
                    Intent intent = new Intent(requireContext(), LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    requireActivity().finish();
                })
                .setNegativeButton(R.string. no, null)
                .show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
