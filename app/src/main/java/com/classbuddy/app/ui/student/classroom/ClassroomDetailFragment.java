package com.classbuddy.app.ui.student.classroom;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.classbuddy.app.R;
import com.classbuddy.app.databinding.FragmentClassroomDetailBinding;
import com.classbuddy.app.util.CodeGenerator;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class ClassroomDetailFragment extends Fragment {

    private FragmentClassroomDetailBinding binding;
    private ClassroomDetailViewModel viewModel;

    private String classroomId;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentClassroomDetailBinding.inflate(inflater, container, false);
        return binding. getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getArguments() != null) {
            classroomId = getArguments().getString("classroomId");
        }

        if (classroomId == null) {
            Toast.makeText(requireContext(), "Classroom not found", Toast.LENGTH_SHORT).show();
            Navigation.findNavController(view).navigateUp();
            return;
        }

        viewModel = new ViewModelProvider(this).get(ClassroomDetailViewModel.class);
        viewModel.loadClassroom(classroomId);

        setupClickListeners();
        observeViewModel();
    }

    private void setupClickListeners() {
        binding.toolbar.setNavigationOnClickListener(v -> {
            Navigation.findNavController(v).navigateUp();
        });

        binding.cardRoutine.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putString("classroomId", classroomId);
            Navigation.findNavController(v).navigate(R.id.action_classroomDetail_to_routine, bundle);
        });

        binding.cardExams.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putString("classroomId", classroomId);
            Navigation.findNavController(v).navigate(R.id.action_classroomDetail_to_exams, bundle);
        });

        binding.cardNotices.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putString("classroomId", classroomId);
            Navigation.findNavController(v).navigate(R.id.action_classroomDetail_to_notices, bundle);
        });

        binding.btnLeaveClassroom.setOnClickListener(v -> {
            showLeaveConfirmation();
        });

        binding.swipeRefresh.setOnRefreshListener(() -> {
            viewModel.loadClassroom(classroomId);
        });
    }

    private void showLeaveConfirmation() {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.leave_classroom)
                .setMessage(R. string.leave_classroom_confirm)
                .setPositiveButton(R.string.leave, (dialog, which) -> {
                    viewModel.leaveClassroom(classroomId);
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void observeViewModel() {
        viewModel. getClassroom().observe(getViewLifecycleOwner(), resource -> {
            binding.swipeRefresh.setRefreshing(false);

            switch (resource.status) {
                case LOADING:
                    binding.progressBar.setVisibility(View. VISIBLE);
                    binding.layoutContent.setVisibility(View. GONE);
                    break;

                case SUCCESS:
                    binding.progressBar.setVisibility(View.GONE);
                    if (resource.data != null) {
                        binding.layoutContent. setVisibility(View.VISIBLE);
                        populateData(resource.data);
                    }
                    break;

                case ERROR:
                    binding.progressBar.setVisibility(View. GONE);
                    Toast.makeText(requireContext(), resource.message, Toast.LENGTH_SHORT).show();
                    break;
            }
        });

        viewModel.getLeaveResult().observe(getViewLifecycleOwner(), resource -> {
            if (resource.isSuccess()) {
                Toast.makeText(requireContext(), R.string.left_classroom, Toast.LENGTH_SHORT).show();
                Navigation.findNavController(requireView()).navigateUp();
            } else if (resource.isError()) {
                Toast.makeText(requireContext(), resource.message, Toast.LENGTH_SHORT).show();
            }
        });

        viewModel.getStats().observe(getViewLifecycleOwner(), stats -> {
            if (stats != null) {
                binding.tvRoutineCount.setText(stats.routineCount + " Classes/Week");
                binding.tvExamCount.setText(stats.examCount + " Upcoming");
                binding.tvNoticeCount.setText(stats.noticeCount + " Active");
            }
        });
    }

    private void populateData(com.classbuddy.app.data.model.Classroom classroom) {
        binding.toolbar.setTitle(classroom.getName());
        binding.tvClassName.setText(classroom.getName());
        binding.tvSection.setText(classroom.getSection() + " â€¢ " + classroom.getDepartment());
        binding.tvDescription.setText(classroom.getDescription());
        binding.tvAdminName.setText("Managed by " + classroom.getAdminName());
        binding.tvStudentCount.setText(classroom.getStudentCount() + " Students");
        binding.tvCode.setText("Code: " + CodeGenerator.formatCodeForDisplay(classroom.getCode()));

        // Set initial letter
        String initial = classroom.getName().substring(0, 1).toUpperCase();
        binding.tvInitial.setText(initial);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
