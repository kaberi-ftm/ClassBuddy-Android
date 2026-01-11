package com.classbuddy.app.ui.admin.student;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.classbuddy.app.R;
import com.classbuddy.app.adapter.StudentAdapter;
import com.classbuddy.app.data.model.Classroom;
import com.classbuddy.app.data.model.User;
import com.classbuddy.app.ui.admin.student.StudentManagementViewModel;
import com.classbuddy.app.databinding.FragmentStudentManagementBinding;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;
import java.util.List;

public class StudentManagementFragment extends Fragment {

    private FragmentStudentManagementBinding binding;
    private StudentManagementViewModel viewModel;
    private StudentAdapter adapter;

    private List<Classroom> classrooms = new ArrayList<>();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentStudentManagementBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(StudentManagementViewModel.class);

        setupRecyclerView();
        setupClickListeners();
        observeViewModel();
    }

    private void setupRecyclerView() {
        adapter = new StudentAdapter(new StudentAdapter.OnStudentActionListener() {
            @Override
            public void onStudentClick(User student) {
                showStudentDetails(student);
            }

            @Override
            public void onRemoveClick(User student, int position) {
                showRemoveConfirmation(student);
            }

            @Override
            public void onMessageClick(User student) {
                // Open email or messaging app
                sendEmail(student. getEmail());
            }
        });

        binding.rvStudents.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvStudents.setAdapter(adapter);
    }

    private void setupClickListeners() {
        binding.swipeRefresh.setOnRefreshListener(() -> {
            viewModel.refreshStudents();
        });
    }

    private void showStudentDetails(User student) {
        String message = "Email: " + student.getEmail();
        if (student.getCreatedAt() != null) {
            message += "\nJoined: " + com.classbuddy.app.util.DateTimeUtils.formatDate(student.getCreatedAt());
        }

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(student.getFullName())
                .setMessage(message)
                .setPositiveButton(R.string.ok, null)
                .setNeutralButton("Send Email", (dialog, which) -> {
                    sendEmail(student.getEmail());
                })
                .show();
    }

    private void showRemoveConfirmation(User student) {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Remove Student")
                .setMessage("Remove " + student.getFullName() + " from this classroom?")
                .setPositiveButton(R.string.yes, (dialog, which) -> {
                    viewModel.removeStudent(student. getId());
                })
                .setNegativeButton(R. string.no, null)
                .show();
    }

    private void sendEmail(String email) {
        android.content.Intent intent = new android.content.Intent(android.content.Intent.ACTION_SENDTO);
        intent.setData(android.net.Uri.parse("mailto:" + email));
        intent.putExtra(android.content.Intent. EXTRA_SUBJECT, "Message from ClassBuddy");

        if (intent.resolveActivity(requireActivity().getPackageManager()) != null) {
            startActivity(intent);
        } else {
            Toast.makeText(requireContext(), "No email app found", Toast.LENGTH_SHORT).show();
        }
    }

    private void observeViewModel() {
        viewModel.getClassrooms().observe(getViewLifecycleOwner(), resource -> {
            if (resource. isSuccess() && resource.data != null) {
                classrooms = resource.data;
                setupClassroomSpinner();
            }
        });

        viewModel. getStudents().observe(getViewLifecycleOwner(), resource -> {
            binding.swipeRefresh.setRefreshing(false);

            switch (resource.status) {
                case LOADING:
                    binding.progressBar.setVisibility(View.VISIBLE);
                    break;

                case SUCCESS:
                    binding.progressBar.setVisibility(View.GONE);
                    if (resource.data != null && ! resource.data.isEmpty()) {
                        binding.rvStudents.setVisibility(View. VISIBLE);
                        binding.layoutEmpty.setVisibility(View. GONE);
                        adapter. submitList(resource.data);
                        binding.tvStudentCount.setText(resource.data. size() + " Students");
                    } else {
                        binding. rvStudents.setVisibility(View.GONE);
                        binding.layoutEmpty.setVisibility(View.VISIBLE);
                        binding.tvStudentCount. setText("0 Students");
                    }
                    break;

                case ERROR:
                    binding.progressBar.setVisibility(View. GONE);
                    binding.layoutEmpty.setVisibility(View. VISIBLE);
                    Toast.makeText(requireContext(), resource.message, Toast.LENGTH_SHORT).show();
                    break;
            }
        });

        viewModel.getRemoveResult().observe(getViewLifecycleOwner(), resource -> {
            if (resource.isSuccess()) {
                Toast.makeText(requireContext(), "Student removed", Toast.LENGTH_SHORT).show();
            } else if (resource.isError()) {
                Toast.makeText(requireContext(), resource.message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupClassroomSpinner() {
        List<String> classroomNames = new ArrayList<>();
        classroomNames.add("All Classrooms");
        for (Classroom classroom : classrooms) {
            classroomNames.add(classroom.getName());
        }

        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(
                requireContext(),
                android. R.layout.simple_spinner_item,
                classroomNames
        );
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerClassroom.setAdapter(spinnerAdapter);

        binding.spinnerClassroom.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    viewModel.loadAllStudents();
                } else {
                    Classroom selected = classrooms.get(position - 1);
                    viewModel.loadStudentsForClassroom(selected);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<? > parent) {}
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
