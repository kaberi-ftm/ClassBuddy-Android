package com.classbuddy.app.ui.splash;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.classbuddy.app.R;
import com.classbuddy.app.databinding.ActivitySplashBinding;
import com.classbuddy.app.ui.admin.AdminMainActivity;
import com.classbuddy.app.ui.auth.LoginActivity;
import com.classbuddy.app.ui.student.StudentMainActivity;
import com.classbuddy.app.util.Constants;

@SuppressLint("CustomSplashScreen")
public class SplashActivity extends AppCompatActivity {

    private static final long SPLASH_DELAY = 2000; // 2 seconds
    private ActivitySplashBinding binding;
    private SplashViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySplashBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(SplashViewModel.class);

        new Handler(Looper.getMainLooper())
                .postDelayed(this::checkAuthState, SPLASH_DELAY);
    }

    private void checkAuthState() {
        if (viewModel.isLoggedIn()) {
            viewModel.getCurrentUser().observe(this, resource -> {
                if (resource.isSuccess() && resource.data != null) {
                    navigateBasedOnRole(resource.data.getRole());
                } else {
                    navigateToLogin();
                }
            });
        } else {
            navigateToLogin();
        }
    }

    private void navigateBasedOnRole(String role) {
        Intent intent;
        if (Constants.ROLE_ADMIN.equalsIgnoreCase(role)) {
            intent = new Intent(this, AdminMainActivity.class);
        } else {
            intent = new Intent(this, StudentMainActivity.class);
        }
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void navigateToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
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
