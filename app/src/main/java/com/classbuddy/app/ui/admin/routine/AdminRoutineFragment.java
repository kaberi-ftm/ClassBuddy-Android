package com.classbuddy.app.ui.admin.routine;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.classbuddy.app.R;
import com.classbuddy.app.data.model.Routine;
import com.classbuddy.app.databinding.FragmentAdminRoutineBinding;
import com.classbuddy.app.util.Constants;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.tabs.TabLayout;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class AdminRoutineFragment extends Fragment {

    private FragmentAdminRoutineBinding binding;
    private AdminRoutineViewModel viewModel;
    private AdminRoutineAdapter adapter;

    private String classroomId;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentAdminRoutineBinding.inflate(inflater, container, false);
        return binding. getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getArguments() != null) {
            classroomId = getArguments().getString("classroomId");
        }

        if (classroomId == null) {
            Toast.makeText(requireContext(), "Classroom ID required", Toast.LENGTH_SHORT).show();
            Navigation.findNavController(view).navigateUp();
            return;
        }

        viewModel = new ViewModelProvider(this).get(AdminRoutineViewModel.class);
        viewModel.setClassroomId(classroomId);

        setupTabs();
        setupRecyclerView();
        setupClickListeners();
        observeViewModel();
    }

    private void setupTabs() {
        for (String day : Constants.DAYS_OF_WEEK) {
            binding. tabLayout.addTab(binding. tabLayout.newTab().setText(day. substring(0, 3)));
        }

        binding.tabLayout.addOnTabSelectedListener(new TabLayout. OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewModel.filterByDay(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void setupRecyclerView() {
        adapter = new AdminRoutineAdapter(new AdminRoutineAdapter.OnRoutineActionListener() {
            @Override
            public void onEditClick(Routine routine) {
                Bundle bundle = new Bundle();
                bundle.putString("classroomId", classroomId);
                bundle.putString("routineId", routine.getId());
                Navigation.findNavController(requireView())
                        .navigate(R.id.action_routine_to_addRoutine, bundle);
            }

            @Override
            public void onDeleteClick(Routine routine) {
                showDeleteConfirmation(routine);
            }

            @Override
            public void onCancelClick(Routine routine) {
                if (routine.isCancelled()) {
                    showRestoreConfirmation(routine);
                } else {
                    showCancelClassDialog(routine);
                }
            }
        });

        binding.rvRoutine.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvRoutine.setAdapter(adapter);
    }

    private void showCancelClassDialog(Routine routine) {
        Calendar calendar = Calendar.getInstance();
        
        // Find next occurrence of this day
        int targetDay = routine.getDayIndex();
        int currentDay = calendar.get(Calendar.DAY_OF_WEEK) - 1; // Calendar.SUNDAY = 1
        int daysUntil = (targetDay - currentDay + 7) % 7;
        if (daysUntil == 0 && calendar.get(Calendar.HOUR_OF_DAY) > 12) {
            daysUntil = 7; // If it's today but late, go to next week
        }
        calendar.add(Calendar.DAY_OF_MONTH, daysUntil);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                requireContext(),
                (view, year, month, dayOfMonth) -> {
                    Calendar selectedDate = Calendar.getInstance();
                    selectedDate.set(year, month, dayOfMonth);
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                    String dateStr = sdf.format(selectedDate.getTime());
                    
                    showReasonDialog(routine, dateStr);
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.setTitle(R.string.select_cancel_date);
        datePickerDialog.show();
    }

    private void showReasonDialog(Routine routine, String date) {
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_cancel_reason, null);
        EditText etReason = dialogView.findViewById(R.id.etReason);

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.cancel_class)
                .setMessage(getString(R.string.cancel_class_confirm) + "\nDate: " + date)
                .setView(dialogView)
                .setPositiveButton(R.string.cancel_class, (dialog, which) -> {
                    String reason = etReason.getText().toString().trim();
                    viewModel.cancelClass(routine, reason, date);
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void showRestoreConfirmation(Routine routine) {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.restore_class)
                .setMessage("Do you want to restore this class?")
                .setPositiveButton(R.string.restore_class, (dialog, which) -> {
                    viewModel.restoreClass(routine.getId());
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void setupClickListeners() {
        binding.toolbar.setNavigationOnClickListener(v -> {
            Navigation.findNavController(v).navigateUp();
        });

        binding.fabAddRoutine.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle. putString("classroomId", classroomId);
            Navigation.findNavController(v)
                    .navigate(R. id.action_routine_to_addRoutine, bundle);
        });

        binding.swipeRefresh.setOnRefreshListener(() -> {
            viewModel.loadRoutines();
        });
    }

    private void showDeleteConfirmation(Routine routine) {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.delete)
                .setMessage("Delete " + routine.getSubject() + " from routine?")
                .setPositiveButton(R.string.delete, (dialog, which) -> {
                    viewModel.deleteRoutine(routine. getId());
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void observeViewModel() {
        viewModel.getFilteredRoutines().observe(getViewLifecycleOwner(), resource -> {
            binding.swipeRefresh.setRefreshing(false);

            switch (resource.status) {
                case LOADING:
                    binding.progressBar.setVisibility(View.VISIBLE);
                    break;

                case SUCCESS:
                    binding.progressBar.setVisibility(View.GONE);
                    if (resource.data != null && ! resource.data.isEmpty()) {
                        binding.rvRoutine.setVisibility(View.VISIBLE);
                        binding.layoutEmpty.setVisibility(View. GONE);
                        adapter.submitList(resource.data);
                    } else {
                        binding. rvRoutine.setVisibility(View.GONE);
                        binding.layoutEmpty.setVisibility(View.VISIBLE);
                    }
                    break;

                case ERROR:
                    binding.progressBar.setVisibility(View.GONE);
                    binding.layoutEmpty.setVisibility(View. VISIBLE);
                    Toast.makeText(requireContext(), resource.message, Toast.LENGTH_SHORT).show();
                    break;
            }
        });

        viewModel.getDeleteResult().observe(getViewLifecycleOwner(), resource -> {
            if (resource.isSuccess()) {
                Toast.makeText(requireContext(), R.string.routine_deleted, Toast.LENGTH_SHORT).show();
            } else if (resource.isError()) {
                Toast.makeText(requireContext(), resource.message, Toast.LENGTH_SHORT).show();
            }
        });

        viewModel.getCancelResult().observe(getViewLifecycleOwner(), resource -> {
            if (resource.isSuccess()) {
                Toast.makeText(requireContext(), R.string.class_cancelled, Toast.LENGTH_SHORT).show();
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
