package com.classbuddy.app.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.classbuddy.app.databinding.ActivityLoginBinding;
import com.classbuddy.app.ui.admin.AdminMainActivity;
import com.classbuddy.app.ui.student.StudentMainActivity;
import com.classbuddy.app.util.Constants;
import com.classbuddy.app.util.ValidationUtils;

public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;
    private LoginViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(LoginViewModel.class);

        setupClickListeners();
        observeViewModel();
    }

    private void setupClickListeners() {
        binding.btnLogin.setOnClickListener(v -> attemptLogin());

        binding.tvRegister.setOnClickListener(v -> {
            startActivity(new Intent(this, RegisterActivity. class));
        });

        binding.tvForgotPassword.setOnClickListener(v -> {
            String email = binding.etEmail.getText().toString().trim();
            if (ValidationUtils. isValidEmail(email)) {
                viewModel.sendPasswordResetEmail(email).observe(this, resource -> {
                    if (resource.isSuccess()) {
                        Toast.makeText(this, "Password reset email sent", Toast.LENGTH_SHORT).show();
                    } else if (resource.isError()) {
                        Toast.makeText(this, resource.message, Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                binding.tilEmail.setError("Enter a valid email first");
            }
        });
    }

    private void attemptLogin() {
        String email = binding.etEmail.getText().toString().trim();
        String password = binding.etPassword. getText().toString().trim();

        // Clear previous errors
        binding.tilEmail.setError(null);
        binding.tilPassword.setError(null);

        // Validate inputs
        boolean isValid = true;

        String emailError = ValidationUtils. getEmailError(email);
        if (emailError != null) {
            binding.tilEmail.setError(emailError);
            isValid = false;
        }

        String passwordError = ValidationUtils.getPasswordError(password);
        if (passwordError != null) {
            binding.tilPassword.setError(passwordError);
            isValid = false;
        }

        if (isValid) {
            viewModel. login(email, password);
        }
    }

    private void observeViewModel() {
        viewModel. getLoginResult().observe(this, resource -> {
            switch (resource.status) {
                case LOADING:
                    showLoading(true);
                    break;
                case SUCCESS:
                    showLoading(false);
                    if (resource.data != null) {
                        navigateBasedOnRole(resource.data.getRole());
                    }
                    break;
                case ERROR:
                    showLoading(false);
                    Toast.makeText(this, resource.message, Toast. LENGTH_LONG).show();
                    break;
            }
        });
    }

    private void showLoading(boolean isLoading) {
        binding.progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        binding.btnLogin.setEnabled(! isLoading);
        binding.etEmail.setEnabled(! isLoading);
        binding.etPassword.setEnabled(!isLoading);
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
