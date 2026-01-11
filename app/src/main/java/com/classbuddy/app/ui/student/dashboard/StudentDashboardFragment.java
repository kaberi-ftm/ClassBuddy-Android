package com.classbuddy.app.ui.student.dashboard;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.classbuddy.app.R;
import com.classbuddy.app.adapter.ExamAdapter;
import com.classbuddy.app.adapter.NoticeAdapter;
import com.classbuddy.app.adapter.RoutineAdapter;
import com.classbuddy.app.databinding.FragmentStudentDashboardBinding;
import com.classbuddy.app.util.DateTimeUtils;

public class StudentDashboardFragment extends Fragment {

    private FragmentStudentDashboardBinding binding;
    private StudentDashboardViewModel viewModel;
    private RoutineAdapter routineAdapter;
    private ExamAdapter examAdapter;
    private NoticeAdapter noticeAdapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentStudentDashboardBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(StudentDashboardViewModel.class);

        setupUI();
        setupRecyclerViews();
        setupClickListeners();
        observeViewModel();
    }

    private void setupUI() {
        binding.tvDate.setText(DateTimeUtils. formatDayDate(new java.util.Date()));
    }

    private void setupRecyclerViews() {
        // Today's Classes
        routineAdapter = new RoutineAdapter(routine -> {
            // Handle routine click
        });
        binding. rvTodayClasses. setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvTodayClasses.setAdapter(routineAdapter);
        binding.rvTodayClasses.setNestedScrollingEnabled(false);

        // Upcoming Exams
        examAdapter = new ExamAdapter(exam -> {
            // Handle exam click
        });
        binding. rvUpcomingExams.setLayoutManager(
                new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
        binding.rvUpcomingExams. setAdapter(examAdapter);

        // Recent Notices
        noticeAdapter = new NoticeAdapter((notice, position) -> {
            // Handle notice click
        });
        binding. rvRecentNotices.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvRecentNotices.setAdapter(noticeAdapter);
        binding.rvRecentNotices.setNestedScrollingEnabled(false);
    }

    private void setupClickListeners() {
        binding.cardJoinClassroom.setOnClickListener(v -> {
            Navigation.findNavController(v).navigate(
                    R.id.action_dashboard_to_joinClassroom);
        });

        binding.cardViewSchedule.setOnClickListener(v -> {
            Navigation.findNavController(v).navigate(R.id.studentRoutineFragment);
        });

        binding.tvSeeAllExams.setOnClickListener(v -> {
            Navigation.findNavController(v).navigate(R.id.studentExamFragment);
        });

        binding.tvSeeAllNotices.setOnClickListener(v -> {
            Navigation.findNavController(v).navigate(R.id.studentNoticeFragment);
        });

        binding.swipeRefresh.setOnRefreshListener(() -> {
            viewModel.refreshData();
        });
    }

    private void observeViewModel() {
        // User data
        viewModel.getCurrentUser().observe(getViewLifecycleOwner(), resource -> {
            if (resource. isSuccess() && resource.data != null) {
                binding.tvGreeting.setText("Hello, " + resource.data.getFullName() + "!");

                if (resource.data. getProfileImageUrl() != null) {
                    Glide.with(this)
                            . load(resource.data. getProfileImageUrl())
                            .placeholder(R.drawable. ic_profile)
                            . circleCrop()
                            .into(binding.ivProfile);
                }
            }
        });

        // Today's routine
        viewModel. getTodaysRoutine().observe(getViewLifecycleOwner(), resource -> {
            binding.swipeRefresh.setRefreshing(false);

            if (resource. isSuccess() && resource.data != null) {
                if (resource.data. isEmpty()) {
                    binding. rvTodayClasses.setVisibility(View.GONE);
                    binding.layoutNoClasses.setVisibility(View. VISIBLE);
                } else {
                    binding.rvTodayClasses.setVisibility(View.VISIBLE);
                    binding. layoutNoClasses. setVisibility(View.GONE);
                    routineAdapter.submitList(resource.data);
                }
            }
        });

        // Upcoming exams
        viewModel.getUpcomingExams().observe(getViewLifecycleOwner(), resource -> {
            if (resource.isSuccess() && resource.data != null) {
                if (resource.data. isEmpty()) {
                    binding.rvUpcomingExams.setVisibility(View.GONE);
                    binding. tvNoExams.setVisibility(View. VISIBLE);
                } else {
                    binding.rvUpcomingExams. setVisibility(View.VISIBLE);
                    binding.tvNoExams.setVisibility(View. GONE);
                    examAdapter.submitList(resource.data);
                }
            }
        });

        // Recent notices
        viewModel. getRecentNotices().observe(getViewLifecycleOwner(), resource -> {
            if (resource.isSuccess() && resource.data != null) {
                if (resource.data. isEmpty()) {
                    binding.rvRecentNotices. setVisibility(View.GONE);
                    binding.tvNoNotices.setVisibility(View.VISIBLE);
                } else {
                    binding.rvRecentNotices.setVisibility(View. VISIBLE);
                    binding.tvNoNotices.setVisibility(View.GONE);
                    noticeAdapter.submitList(resource.data);
                }
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
