package com.classbuddy.app.ui.student.routine;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.classbuddy.app.R;
import com.classbuddy.app.adapter.GroupedRoutineAdapter;
import com.classbuddy.app.databinding.FragmentStudentRoutineBinding;
import com.classbuddy.app.util.Constants;
import com.classbuddy.app.util.DateTimeUtils;
import com.google.android.material.tabs.TabLayout;

public class StudentRoutineFragment extends Fragment {

    private FragmentStudentRoutineBinding binding;
    private StudentRoutineViewModel viewModel;
    private GroupedRoutineAdapter adapter;

    private String classroomId = null;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentStudentRoutineBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Get classroomId from arguments if available
        if (getArguments() != null) {
            classroomId = getArguments().getString("classroomId");
        }

        viewModel = new ViewModelProvider(this).get(StudentRoutineViewModel.class);

        setupTabs();
        setupRecyclerView();
        setupClickListeners();
        observeViewModel();

        // Load routines
        if (classroomId != null) {
            viewModel.loadRoutinesForClassroom(classroomId);
        } else {
            viewModel.loadAllRoutines();
        }
    }

    private void setupTabs() {
        // Add day tabs
        for (String day : Constants.DAYS_OF_WEEK) {
            binding.tabLayout.addTab(binding.tabLayout.newTab().setText(day.substring(0, 3)));
        }

        // Select current day
        int currentDayIndex = DateTimeUtils.getCurrentDayIndex();
        TabLayout.Tab tab = binding.tabLayout.getTabAt(currentDayIndex);
        if (tab != null) {
            tab.select();
        }

        binding.tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewModel.filterByDay(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void setupRecyclerView() {
        adapter = new GroupedRoutineAdapter(routine -> {
            // Handle routine click - show details
            showRoutineDetails(routine);
        });

        binding.rvRoutine.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvRoutine.setAdapter(adapter);
    }

    private void setupClickListeners() {
        binding.toolbar.setNavigationOnClickListener(v -> {
            requireActivity().onBackPressed();
        });

        binding.swipeRefresh.setOnRefreshListener(() -> {
            if (classroomId != null) {
                viewModel.loadRoutinesForClassroom(classroomId);
            } else {
                viewModel.loadAllRoutines();
            }
        });
    }

    private void showRoutineDetails(com.classbuddy.app.data.model.Routine routine) {
        new com.google.android.material.dialog.MaterialAlertDialogBuilder(requireContext())
                .setTitle(routine.getSubject())
                .setMessage(
                        "Faculty: " + routine.getFaculty() + "\n" +
                                "Room: " + routine.getRoom() + "\n" +
                                "Time: " + DateTimeUtils.formatTime(routine.getStartTime()) +
                                " - " + DateTimeUtils.formatTime(routine.getEndTime()) + "\n" +
                                "Type: " + routine.getType().toUpperCase() + "\n" +
                                "Classroom: " + routine.getClassroomName()
                )
                .setPositiveButton(R.string.ok, null)
                .show();
    }

    private void observeViewModel() {
        viewModel.getFilteredRoutines().observe(getViewLifecycleOwner(), resource -> {
            binding.swipeRefresh.setRefreshing(false);

            switch (resource.status) {
                case LOADING:
                    binding.progressBar.setVisibility(View.VISIBLE);
                    binding.layoutEmpty.setVisibility(View.GONE);
                    break;

                case SUCCESS:
                    binding.progressBar.setVisibility(View.GONE);
                    if (resource.data != null && !resource.data.isEmpty()) {
                        binding.rvRoutine.setVisibility(View.VISIBLE);
                        binding.layoutEmpty.setVisibility(View.GONE);
                        adapter.submitList(resource.data);
                    } else {
                        binding.rvRoutine.setVisibility(View.GONE);
                        binding.layoutEmpty.setVisibility(View.VISIBLE);
                    }
                    break;

                case ERROR:
                    binding.progressBar.setVisibility(View.GONE);
                    binding.layoutEmpty.setVisibility(View.VISIBLE);
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
