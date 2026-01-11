package com.classbuddy.app.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.RadioButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.classbuddy.app.R;
import com.classbuddy.app.databinding.ActivityRegisterBinding;
import com.classbuddy.app.ui.admin.AdminMainActivity;
import com.classbuddy.app.ui.student.StudentMainActivity;
import com.classbuddy.app.util.Constants;
import com.classbuddy.app.util.ValidationUtils;

public class RegisterActivity extends AppCompatActivity {

    private ActivityRegisterBinding binding;
    private RegisterViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(RegisterViewModel.class);

        setupClickListeners();
        observeViewModel();
    }

    private void setupClickListeners() {
        binding.btnRegister.setOnClickListener(v -> attemptRegister());

        binding.tvLogin.setOnClickListener(v -> {
            finish(); // Go back to login
        });

        binding.ivBack.setOnClickListener(v -> finish());
    }

    private void attemptRegister() {
        String fullName = binding.etFullName.getText().toString().trim();
        String email = binding.etEmail.getText().toString().trim();
        String password = binding.etPassword. getText().toString().trim();
        String confirmPassword = binding.etConfirmPassword.getText().toString().trim();

        // Get selected role
        int selectedRoleId = binding.rgRole.getCheckedRadioButtonId();
        String role = null;
        if (selectedRoleId == R.id.rbAdmin) {
            role = Constants.ROLE_ADMIN;
        } else if (selectedRoleId == R.id.rbStudent) {
            role = Constants. ROLE_STUDENT;
        }

        // Clear previous errors
        binding.tilFullName.setError(null);
        binding.tilEmail.setError(null);
        binding.tilPassword.setError(null);
        binding.tilConfirmPassword. setError(null);
        binding.tvRoleError.setVisibility(View. GONE);

        // Validate inputs
        boolean isValid = true;

        String nameError = ValidationUtils. getNameError(fullName);
        if (nameError != null) {
            binding.tilFullName. setError(nameError);
            isValid = false;
        }

        String emailError = ValidationUtils. getEmailError(email);
        if (emailError != null) {
            binding. tilEmail.setError(emailError);
            isValid = false;
        }

        String passwordError = ValidationUtils.getPasswordError(password);
        if (passwordError != null) {
            binding.tilPassword.setError(passwordError);
            isValid = false;
        }

        String confirmPasswordError = ValidationUtils.getConfirmPasswordError(password, confirmPassword);
        if (confirmPasswordError != null) {
            binding.tilConfirmPassword.setError(confirmPasswordError);
            isValid = false;
        }

        if (role == null) {
            binding.tvRoleError.setVisibility(View.VISIBLE);
            binding. tvRoleError. setText(R.string.error_select_role);
            isValid = false;
        }

        if (isValid) {
            viewModel.register(email, password, fullName, role);
        }
    }

    private void observeViewModel() {
        viewModel.getRegisterResult().observe(this, resource -> {
            switch (resource.status) {
                case LOADING:
                    showLoading(true);
                    break;
                case SUCCESS:
                    showLoading(false);
                    Toast.makeText(this, R.string.register_success, Toast. LENGTH_SHORT).show();
                    if (resource.data != null) {
                        navigateBasedOnRole(resource. data.getRole());
                    }
                    break;
                case ERROR:
                    showLoading(false);
                    Toast.makeText(this, resource. message, Toast.LENGTH_LONG).show();
                    break;
            }
        });
    }

    private void showLoading(boolean isLoading) {
        binding.progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        binding.btnRegister.setEnabled(! isLoading);
        binding.etFullName.setEnabled(! isLoading);
        binding.etEmail.setEnabled(!isLoading);
        binding.etPassword. setEnabled(!isLoading);
        binding.etConfirmPassword.setEnabled(!isLoading);
        binding.rbAdmin.setEnabled(!isLoading);
        binding.rbStudent.setEnabled(! isLoading);
    }

    private void navigateBasedOnRole(String role) {
        Intent intent;
        if (Constants.ROLE_ADMIN.equalsIgnoreCase(role)) {
            intent = new Intent(this, AdminMainActivity. class);
        } else {
            intent = new Intent(this, StudentMainActivity. class);
        }
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}
