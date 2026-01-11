package com.classbuddy.app.ui.admin.exam;

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
import androidx.recyclerview.widget.LinearLayoutManager;

import com.classbuddy.app.R;
import com.classbuddy.app.adapter.ExamAdapter;
import com.classbuddy.app.data.model.Exam;
import com.classbuddy.app.databinding.FragmentAdminExamBinding;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class AdminExamFragment extends Fragment {

    private FragmentAdminExamBinding binding;
    private AdminExamViewModel viewModel;
    private ExamAdapter adapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentAdminExamBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(AdminExamViewModel.class);

        setupRecyclerView();
        setupClickListeners();
        observeViewModel();
    }

    private void setupRecyclerView() {
        adapter = new ExamAdapter(exam -> {
            // Navigate to edit exam
            Bundle bundle = new Bundle();
            bundle.putString("examId", exam.getId());
            Navigation.findNavController(requireView())
                    .navigate(R.id.action_exams_to_createExam, bundle);
        });

        binding.rvExams.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding. rvExams.setAdapter(adapter);
    }

    private void setupClickListeners() {
        binding.fabCreateExam.setOnClickListener(v -> {
            Navigation.findNavController(v)
                    .navigate(R.id. action_exams_to_createExam);
        });

        binding.swipeRefresh.setOnRefreshListener(() -> viewModel.refreshExams());

        binding.btnCreateFirst.setOnClickListener(v -> {
            Navigation.findNavController(v)
                    .navigate(R. id.action_exams_to_createExam);
        });

        // Filter chips
        binding.chipAll.setOnClickListener(v -> viewModel.setFilter(null));
        binding.chipCt.setOnClickListener(v -> viewModel.setFilter("ct"));
        binding.chipFinal.setOnClickListener(v -> viewModel.setFilter("final"));
        binding.chipLabQuiz.setOnClickListener(v -> viewModel.setFilter("labquiz"));
        binding.chipViva.setOnClickListener(v -> viewModel.setFilter("viva"));
    }

    private void observeViewModel() {
        viewModel. getExams().observe(getViewLifecycleOwner(), resource -> {
            binding.swipeRefresh.setRefreshing(false);

            switch (resource. status) {
                case LOADING:
                    binding.progressBar.setVisibility(View.VISIBLE);
                    binding.layoutEmpty.setVisibility(View.GONE);
                    break;

                case SUCCESS:
                    binding.progressBar.setVisibility(View.GONE);
                    if (resource.data != null && ! resource.data.isEmpty()) {
                        binding. rvExams.setVisibility(View. VISIBLE);
                        binding.layoutEmpty.setVisibility(View. GONE);
                        adapter.submitList(resource.data);
                    } else {
                        binding.rvExams.setVisibility(View.GONE);
                        binding. layoutEmpty.setVisibility(View. VISIBLE);
                    }
                    break;

                case ERROR:
                    binding.progressBar.setVisibility(View. GONE);
                    binding.layoutEmpty.setVisibility(View. VISIBLE);
                    Toast.makeText(requireContext(), resource.message, Toast. LENGTH_SHORT).show();
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
