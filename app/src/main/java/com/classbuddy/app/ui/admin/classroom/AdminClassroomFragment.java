package com.classbuddy.app.ui.admin.classroom;

import android.content.Intent;
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
import com.classbuddy.app.adapter.AdminClassroomAdapter;
import com.classbuddy.app.data.model.Classroom;
import com.classbuddy.app.databinding.FragmentAdminClassroomBinding;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class AdminClassroomFragment extends Fragment {

    private FragmentAdminClassroomBinding binding;
    private AdminClassroomViewModel viewModel;
    private AdminClassroomAdapter adapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentAdminClassroomBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(AdminClassroomViewModel.class);

        setupRecyclerView();
        setupClickListeners();
        observeViewModel();
    }

    private void setupRecyclerView() {
        adapter = new AdminClassroomAdapter(new AdminClassroomAdapter.OnClassroomActionListener() {
            @Override
            public void onClassroomClick(Classroom classroom) {
                Bundle bundle = new Bundle();
                bundle. putString("classroomId", classroom.getId());
                Navigation.findNavController(requireView())
                        .navigate(R. id.action_classrooms_to_routine, bundle);
            }

            @Override
            public void onViewStudentsClick(Classroom classroom) {
                Bundle bundle = new Bundle();
                bundle.putString("classroomId", classroom.getId());
                Navigation.findNavController(requireView())
                        .navigate(R.id.action_classrooms_to_classroomStudents, bundle);
            }

            @Override
            public void onDeleteClick(Classroom classroom) {
                showDeleteConfirmation(classroom);
            }

            @Override
            public void onShareClick(Classroom classroom) {
                shareClassroomCode(classroom);
            }
        });

        binding.rvClassrooms.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvClassrooms.setAdapter(adapter);
    }

    private void setupClickListeners() {
        binding.fabCreateClassroom.setOnClickListener(v -> {
            Navigation.findNavController(v)
                    .navigate(R. id.action_classrooms_to_createClassroom);
        });

        binding.swipeRefresh.setOnRefreshListener(() -> viewModel.refreshClassrooms());

        binding.btnCreateFirst.setOnClickListener(v -> {
            Navigation.findNavController(v)
                    .navigate(R.id.action_classrooms_to_createClassroom);
        });
    }

    private void showDeleteConfirmation(Classroom classroom) {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.delete)
                .setMessage("Are you sure you want to delete '" + classroom.getName() + "'?  This action cannot be undone.")
                .setPositiveButton(R.string.delete, (dialog, which) -> {
                    viewModel.deleteClassroom(classroom. getId());
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void shareClassroomCode(Classroom classroom) {
        String shareMessage = getString(R.string.classroom_code_message,
                classroom.getCode(), classroom.getPassword());

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Join my classroom:  " + classroom.getName());
        shareIntent.putExtra(Intent. EXTRA_TEXT, shareMessage);
        startActivity(Intent.createChooser(shareIntent, "Share Classroom Code"));
    }

    private void observeViewModel() {
        viewModel.getClassrooms().observe(getViewLifecycleOwner(), resource -> {
            binding.swipeRefresh.setRefreshing(false);

            switch (resource.status) {
                case LOADING:
                    binding.progressBar.setVisibility(View. VISIBLE);
                    binding.layoutEmpty.setVisibility(View. GONE);
                    break;

                case SUCCESS:
                    binding.progressBar.setVisibility(View. GONE);
                    if (resource.data != null && ! resource.data.isEmpty()) {
                        binding. rvClassrooms.setVisibility(View.VISIBLE);
                        binding.layoutEmpty.setVisibility(View.GONE);
                        adapter.submitList(resource.data);
                    } else {
                        binding.rvClassrooms.setVisibility(View.GONE);
                        binding.layoutEmpty.setVisibility(View.VISIBLE);
                    }
                    break;

                case ERROR:
                    binding.progressBar.setVisibility(View.GONE);
                    binding.layoutEmpty.setVisibility(View.VISIBLE);
                    Toast.makeText(requireContext(), resource.message, Toast.LENGTH_SHORT).show();
                    break;
            }
        });

        viewModel.getDeleteResult().observe(getViewLifecycleOwner(), resource -> {
            if (resource.isSuccess()) {
                Toast.makeText(requireContext(), R.string.classroom_deleted, Toast.LENGTH_SHORT).show();
            } else if (resource.isError()) {
                Toast.makeText(requireContext(), resource.message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
