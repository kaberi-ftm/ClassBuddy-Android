package com.classbuddy.app.ui.admin;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.classbuddy.app.R;
import com.classbuddy.app.databinding.ActivityAdminMainBinding;

public class AdminMainActivity extends AppCompatActivity {

    private ActivityAdminMainBinding binding;
    private NavController navController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAdminMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupNavigation();
    }

    private void setupNavigation() {
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id. nav_host_fragment);

        if (navHostFragment != null) {
            navController = navHostFragment.getNavController();
            NavigationUI.setupWithNavController(binding.bottomNav, navController);
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        return navController.navigateUp() || super.onSupportNavigateUp();
    }

    @Override
    protected void onDestroy() {
        super. onDestroy();
        binding = null;
    }
}
