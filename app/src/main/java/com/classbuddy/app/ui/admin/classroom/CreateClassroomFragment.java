package com.classbuddy.app.ui.admin.classroom;

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
import com.classbuddy.app.ui.admin.classroom.CreateClassroomViewModel;
import com.classbuddy.app.databinding.FragmentCreateClassroomBinding;
import com.classbuddy.app.util.ValidationUtils;

public class CreateClassroomFragment extends Fragment {

    private FragmentCreateClassroomBinding binding;
    private CreateClassroomViewModel viewModel;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentCreateClassroomBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super. onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(CreateClassroomViewModel.class);

        setupClickListeners();
        observeViewModel();
    }

    private void setupClickListeners() {
        binding.toolbar.setNavigationOnClickListener(v -> {
            Navigation.findNavController(v).navigateUp();
        });

        binding.btnCreate.setOnClickListener(v -> attemptCreate());
    }

    private void attemptCreate() {
        String name = binding.etName.getText().toString().trim();
        String description = binding.etDescription.getText().toString().trim();
        String section = binding.etSection.getText().toString().trim();
        String department = binding.etDepartment.getText().toString().trim();

        // Clear errors
        binding.tilName.setError(null);
        binding.tilSection.setError(null);
        binding.tilDepartment. setError(null);

        boolean isValid = true;

        if (! ValidationUtils.isNotEmpty(name)) {
            binding.tilName.setError("Classroom name is required");
            isValid = false;
        }

        if (!ValidationUtils.isNotEmpty(section)) {
            binding.tilSection.setError("Section is required");
            isValid = false;
        }

        if (! ValidationUtils.isNotEmpty(department)) {
            binding.tilDepartment.setError("Department is required");
            isValid = false;
        }

        if (isValid) {
            viewModel.createClassroom(name, description, section, department);
        }
    }

    private void observeViewModel() {
        viewModel.getCreateResult().observe(getViewLifecycleOwner(), resource -> {
            switch (resource. status) {
                case LOADING:
                    showLoading(true);
                    break;

                case SUCCESS:
                    showLoading(false);
                    Toast.makeText(requireContext(), R.string.classroom_created, Toast.LENGTH_SHORT).show();
                    Navigation. findNavController(requireView()).navigateUp();
                    break;

                case ERROR:
                    showLoading(false);
                    Toast. makeText(requireContext(), resource.message, Toast. LENGTH_LONG).show();
                    break;
            }
        });

        viewModel.getAdminName().observe(getViewLifecycleOwner(), name -> {
            // Admin name loaded
        });
    }

    private void showLoading(boolean isLoading) {
        binding.progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        binding.btnCreate.setEnabled(! isLoading);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
