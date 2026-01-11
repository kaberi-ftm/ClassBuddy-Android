package com.classbuddy.app.ui.student.calendar;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.classbuddy.app.R;
import com.classbuddy.app.adapter.CalendarEventAdapter;
import com.classbuddy.app.data.model.CalendarEvent;
import com.classbuddy.app.databinding.FragmentCalendarBinding;
import com.classbuddy.app.util.DateTimeUtils;
import com.google.android.material.datepicker.MaterialDatePicker;

import java.util.Calendar;
import java.util.Date;

public class CalendarFragment extends Fragment {

    private FragmentCalendarBinding binding;
    private CalendarViewModel viewModel;
    private CalendarEventAdapter adapter;

    private Calendar currentMonth = Calendar.getInstance();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentCalendarBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(CalendarViewModel.class);

        setupCalendar();
        setupRecyclerView();
        setupClickListeners();
        observeViewModel();

        // Load events for current date
        viewModel.loadEventsForDate(new Date());
        updateMonthTitle();
    }

    private void setupCalendar() {
        binding.calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            Calendar selectedDate = Calendar.getInstance();
            selectedDate.set(year, month, dayOfMonth);
            viewModel.loadEventsForDate(selectedDate.getTime());

            binding.tvSelectedDate.setText(DateTimeUtils. formatDayDate(selectedDate.getTime()));
        });

        // Set initial selected date text
        binding.tvSelectedDate. setText(DateTimeUtils.formatDayDate(new Date()));
    }

    private void setupRecyclerView() {
        adapter = new CalendarEventAdapter(event -> {
            showEventDetail(event);
        });

        binding.rvEvents.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvEvents.setAdapter(adapter);
    }

    private void setupClickListeners() {
        binding.btnPrevMonth.setOnClickListener(v -> {
            currentMonth.add(Calendar.MONTH, -1);
            binding.calendarView.setDate(currentMonth.getTimeInMillis());
            updateMonthTitle();
        });

        binding.btnNextMonth.setOnClickListener(v -> {
            currentMonth.add(Calendar.MONTH, 1);
            binding.calendarView.setDate(currentMonth.getTimeInMillis());
            updateMonthTitle();
        });

        binding.tvMonthYear.setOnClickListener(v -> {
            showDatePicker();
        });

        binding.swipeRefresh.setOnRefreshListener(() -> {
            viewModel.refreshEvents();
        });
    }

    private void updateMonthTitle() {
        binding.tvMonthYear.setText(DateTimeUtils.formatMonthYear(currentMonth.getTime()));
    }

    private void showDatePicker() {
        MaterialDatePicker<Long> datePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Select Date")
                .setSelection(currentMonth.getTimeInMillis())
                .build();

        datePicker. addOnPositiveButtonClickListener(selection -> {
            currentMonth.setTimeInMillis(selection);
            binding.calendarView.setDate(selection);
            updateMonthTitle();
            viewModel.loadEventsForDate(currentMonth.getTime());
        });

        datePicker.show(getParentFragmentManager(), "datePicker");
    }

    private void showEventDetail(CalendarEvent event) {
        String message = "Type: " + event.getEventType() + "\n" +
                "Time: " + event.getTime() + "\n" +
                "Classroom: " + event.getClassroomName();

        if (event.getDescription() != null && !event.getDescription().isEmpty()) {
            message += "\n\nDetails: " + event.getDescription();
        }

        new com.google.android.material.dialog.MaterialAlertDialogBuilder(requireContext())
                .setTitle(event.getTitle())
                .setMessage(message)
                .setPositiveButton(R.string.ok, null)
                .show();
    }

    private void observeViewModel() {
        viewModel.getEventsForSelectedDate().observe(getViewLifecycleOwner(), resource -> {
            binding.swipeRefresh.setRefreshing(false);

            if (resource.isSuccess() && resource.data != null) {
                if (resource.data.isEmpty()) {
                    binding.rvEvents.setVisibility(View. GONE);
                    binding.layoutNoEvents.setVisibility(View. VISIBLE);
                } else {
                    binding. rvEvents.setVisibility(View.VISIBLE);
                    binding.layoutNoEvents.setVisibility(View.GONE);
                    adapter.submitList(resource.data);
                }
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
