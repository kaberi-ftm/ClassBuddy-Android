package com.classbuddy.app.ui.admin.notice;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.classbuddy.app.R;
import com.classbuddy.app.data.model.Classroom;
import com.classbuddy.app.databinding.FragmentCreateNoticeBinding;
import com.classbuddy.app.util.Constants;
import com.classbuddy.app.util.ValidationUtils;

import java.util.ArrayList;
import java.util.List;

public class CreateNoticeFragment extends Fragment {

    private FragmentCreateNoticeBinding binding;
    private CreateNoticeViewModel viewModel;

    private String selectedClassroomId = "";
    private String selectedClassroomName = "";
    private String selectedPriority = Constants.PRIORITY_NORMAL;
    private List<Classroom> classrooms = new ArrayList<>();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentCreateNoticeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(CreateNoticeViewModel.class);

        setupViews();
        setupClickListeners();
        observeViewModel();
    }

    private void setupViews() {
        // Hide image attachment button since Storage is not available
        binding.btnAttachImage.setVisibility(View.GONE);
        binding.cardImage.setVisibility(View. GONE);

        // Priority Dropdown
        String[] priorities = {"Low", "Normal", "High", "Urgent"};
        ArrayAdapter<String> priorityAdapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout. simple_dropdown_item_1line,
                priorities
        );
        binding.actvPriority.setAdapter(priorityAdapter);
        binding.actvPriority.setText("Normal", false);

        binding.actvPriority.setOnItemClickListener((parent, v, position, id) -> {
            switch (position) {
                case 0: selectedPriority = Constants.PRIORITY_LOW; break;
                case 1: selectedPriority = Constants.PRIORITY_NORMAL; break;
                case 2: selectedPriority = Constants.PRIORITY_HIGH; break;
                case 3: selectedPriority = Constants.PRIORITY_URGENT; break;
            }
        });
    }

    private void setupClickListeners() {
        binding.toolbar.setNavigationOnClickListener(v -> {
            Navigation.findNavController(v).navigateUp();
        });

        binding.btnPost.setOnClickListener(v -> attemptPost());
    }

    private void attemptPost() {
        String title = binding.etTitle.getText().toString().trim();
        String content = binding.etContent.getText().toString().trim();
        boolean isPinned = binding.switchPin.isChecked();

        // Clear errors
        binding.tilTitle.setError(null);
        binding.tilContent.setError(null);
        binding.tilClassroom.setError(null);

        boolean isValid = true;

        if (! ValidationUtils.isNotEmpty(title)) {
            binding.tilTitle.setError("Title is required");
            isValid = false;
        }

        if (!ValidationUtils.isNotEmpty(content)) {
            binding. tilContent.setError("Content is required");
            isValid = false;
        }

        if (selectedClassroomId.isEmpty()) {
            binding.tilClassroom. setError("Select a classroom");
            isValid = false;
        }

        if (isValid) {
            viewModel. createNotice(
                    selectedClassroomId,
                    selectedClassroomName,
                    title,
                    content,
                    selectedPriority,
                    isPinned,
                    null // No image since Storage is not available
            );
        }
    }

    private void observeViewModel() {
        viewModel.getClassrooms().observe(getViewLifecycleOwner(), resource -> {
            if (resource. isSuccess() && resource.data != null) {
                classrooms = resource.data;
                setupClassroomDropdown();
            }
        });

        viewModel.getAdminName().observe(getViewLifecycleOwner(), name -> {
            // Admin name loaded
        });

        viewModel.getCreateResult().observe(getViewLifecycleOwner(), resource -> {
            switch (resource.status) {
                case LOADING:
                    showLoading(true);
                    break;

                case SUCCESS:
                    showLoading(false);
                    Toast.makeText(requireContext(), R.string.notice_posted, Toast.LENGTH_SHORT).show();
                    Navigation.findNavController(requireView()).navigateUp();
                    break;

                case ERROR:
                    showLoading(false);
                    Toast.makeText(requireContext(), resource.message, Toast.LENGTH_LONG).show();
                    break;
            }
        });
    }

    private void setupClassroomDropdown() {
        List<String> classroomNames = new ArrayList<>();
        for (Classroom classroom : classrooms) {
            classroomNames.add(classroom. getName());
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_dropdown_item_1line,
                classroomNames
        );
        binding.actvClassroom.setAdapter(adapter);

        binding.actvClassroom. setOnItemClickListener((parent, view, position, id) -> {
            Classroom selected = classrooms.get(position);
            selectedClassroomId = selected.getId();
            selectedClassroomName = selected.getName();
        });
    }

    private void showLoading(boolean isLoading) {
        binding.progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        binding.btnPost.setEnabled(! isLoading);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
