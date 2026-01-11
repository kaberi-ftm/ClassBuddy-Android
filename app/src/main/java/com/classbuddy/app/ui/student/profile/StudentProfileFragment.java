package com.classbuddy.app.ui.student.profile;

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

import com.classbuddy.app.R;
import com.classbuddy.app.databinding.FragmentStudentProfileBinding;
import com.classbuddy.app.ui.auth.LoginActivity;
import com.classbuddy.app.util.DateTimeUtils;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class StudentProfileFragment extends Fragment {

    private FragmentStudentProfileBinding binding;
    private StudentProfileViewModel viewModel;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentStudentProfileBinding.inflate(inflater, container, false);
        return binding. getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(StudentProfileViewModel.class);

        setupClickListeners();
        observeViewModel();
    }

    private void setupClickListeners() {
        // Hide the change photo button since Storage is not available
        binding. btnChangePhoto.setVisibility(View.GONE);

        binding.btnEditProfile.setOnClickListener(v -> showEditProfileDialog());

        binding.switchNotifications.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (buttonView.isPressed()) {
                viewModel.updateNotificationSettings(isChecked);
            }
        });

        binding.btnLogout.setOnClickListener(v -> showLogoutDialog());
    }

    private void showEditProfileDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_edit_profile, null);
        com.google.android.material.textfield.TextInputEditText etName =
                dialogView.findViewById(R.id.etName);

        etName.setText(binding.tvName.getText());

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string. edit_profile)
                .setView(dialogView)
                .setPositiveButton(R.string.save, (dialog, which) -> {
                    String newName = etName.getText().toString().trim();
                    if (! newName.isEmpty()) {
                        viewModel.updateProfile(newName);
                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void showLogoutDialog() {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R. string.logout)
                .setMessage(R.string.logout_confirm)
                .setPositiveButton(R.string.yes, (dialog, which) -> {
                    viewModel.logout();
                    Intent intent = new Intent(requireContext(), LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    requireActivity().finish();
                })
                .setNegativeButton(R.string.no, null)
                .show();
    }

    private void observeViewModel() {
        viewModel.getCurrentUser().observe(getViewLifecycleOwner(), resource -> {
            if (resource. isSuccess() && resource.data != null) {
                binding. tvName.setText(resource.data.getFullName());
                binding.tvEmail.setText(resource. data.getEmail());
                binding.tvRole.setText(resource.data.isAdmin() ? "Admin" : "Student");
                binding.switchNotifications.setChecked(resource.data.isNotificationsEnabled());

                if (resource.data.getCreatedAt() != null) {
                    binding.tvJoinedDate.setText(getString(R.string.joined_on,
                            DateTimeUtils.formatDate(resource.data.getCreatedAt())));
                }

                int classroomCount = resource.data. getJoinedClassrooms() != null
                        ? resource.data. getJoinedClassrooms().size() : 0;
                binding.tvClassroomCount.setText(classroomCount + " Classrooms");

                // Default profile icon (no image loading since Storage is not available)
                binding.ivProfile.setImageResource(R.drawable.ic_profile);
            }
        });

        viewModel.getUpdateResult().observe(getViewLifecycleOwner(), resource -> {
            if (resource.isSuccess()) {
                Toast.makeText(requireContext(), R.string.profile_updated, Toast.LENGTH_SHORT).show();
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
