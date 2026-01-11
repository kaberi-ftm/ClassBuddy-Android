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

import com.classbuddy.app.databinding.FragmentJoinClassroomBinding;
import com.classbuddy.app.util.ValidationUtils;

public class JoinClassroomFragment extends Fragment {

    private FragmentJoinClassroomBinding binding;
    private JoinClassroomViewModel viewModel;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentJoinClassroomBinding.inflate(inflater, container, false);
        return binding. getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(JoinClassroomViewModel.class);

        setupClickListeners();
        observeViewModel();
    }

    private void setupClickListeners() {
        binding.toolbar.setNavigationOnClickListener(v -> {
            Navigation.findNavController(v).navigateUp();
        });

        binding.btnJoin.setOnClickListener(v -> attemptJoin());
    }

    private void attemptJoin() {
        String code = binding.etClassroomCode.getText().toString().trim().toUpperCase();
        String password = binding.etPassword.getText().toString().trim();

        // Clear errors
        binding.tilClassroomCode. setError(null);
        binding.tilPassword.setError(null);

        boolean isValid = true;

        if (! ValidationUtils.isValidClassroomCode(code)) {
            binding.tilClassroomCode.setError("Enter a valid 6-character code");
            isValid = false;
        }

        if (! ValidationUtils.isValidClassroomPassword(password)) {
            binding.tilPassword.setError("Password must be at least 4 characters");
            isValid = false;
        }

        if (isValid) {
            viewModel.joinClassroom(code, password);
        }
    }

    private void observeViewModel() {
        viewModel.getJoinResult().observe(getViewLifecycleOwner(), resource -> {
            switch (resource.status) {
                case LOADING:
                    showLoading(true);
                    break;

                case SUCCESS:
                    showLoading(false);
                    Toast.makeText(requireContext(), "Successfully joined classroom!",
                            Toast.LENGTH_SHORT).show();
                    Navigation.findNavController(requireView()).navigateUp();
                    break;

                case ERROR:
                    showLoading(false);
                    Toast. makeText(requireContext(), resource.message, Toast.LENGTH_LONG).show();
                    break;
            }
        });
    }

    private void showLoading(boolean isLoading) {
        binding.progressBar.setVisibility(isLoading ? View. VISIBLE : View. GONE);
        binding.btnJoin.setEnabled(!isLoading);
        binding.etClassroomCode. setEnabled(!isLoading);
        binding.etPassword.setEnabled(!isLoading);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
