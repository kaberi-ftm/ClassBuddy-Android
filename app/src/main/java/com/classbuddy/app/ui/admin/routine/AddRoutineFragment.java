package com.classbuddy.app.ui.admin.routine;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
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
import com.classbuddy.app.databinding.FragmentAddRoutineBinding;
import com.classbuddy.app.util.Constants;
import com.classbuddy.app.util.ValidationUtils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class AddRoutineFragment extends Fragment {

    private FragmentAddRoutineBinding binding;
    private AddRoutineViewModel viewModel;

    private String classroomId;
    private String routineId; // null for new, non-null for edit
    private String selectedStartTime = "";
    private String selectedEndTime = "";
    private int selectedDayIndex = 0;
    private String selectedSpecificDate = null; // For one-time classes
    private boolean isRecurring = true; // Default to recurring

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentAddRoutineBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getArguments() != null) {
            classroomId = getArguments().getString("classroomId");
            routineId = getArguments().getString("routineId");
        }

        if (classroomId == null) {
            Toast.makeText(requireContext(), "Classroom ID required", Toast.LENGTH_SHORT).show();
            Navigation.findNavController(view).navigateUp();
            return;
        }

        viewModel = new ViewModelProvider(this).get(AddRoutineViewModel.class);
        viewModel.setClassroomId(classroomId);

        setupViews();
        setupClickListeners();
        observeViewModel();

        // Update title based on mode
        if (routineId != null) {
            binding.toolbar.setTitle(R.string.edit_routine);
            binding.btnSave.setText(R.string. update);
            viewModel.loadRoutine(routineId);
        }
    }

    private void setupViews() {
        // Schedule Type Dropdown (Recurring/One-time)
        String[] scheduleTypes = {"Weekly Recurring", "Specific Date"};
        ArrayAdapter<String> scheduleAdapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_dropdown_item_1line,
                scheduleTypes
        );
        binding.actvScheduleType.setAdapter(scheduleAdapter);
        binding.actvScheduleType.setText("Weekly Recurring", false);
        binding.actvScheduleType.setOnItemClickListener((parent, v, position, id) -> {
            isRecurring = (position == 0);
            updateScheduleTypeVisibility();
        });

        // Day of Week Dropdown
        ArrayAdapter<String> dayAdapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_dropdown_item_1line,
                Constants.DAYS_OF_WEEK
        );
        binding.actvDay.setAdapter(dayAdapter);
        binding.actvDay.setOnItemClickListener((parent, v, position, id) -> {
            selectedDayIndex = position;
        });

        // Class Type Dropdown
        String[] types = {"Lecture", "Lab", "Tutorial"};
        ArrayAdapter<String> typeAdapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_dropdown_item_1line,
                types
        );
        binding.actvType.setAdapter(typeAdapter);
        binding.actvType.setText("Lecture", false);

        // Initialize visibility
        updateScheduleTypeVisibility();
    }

    private void updateScheduleTypeVisibility() {
        if (isRecurring) {
            binding.tilDay.setVisibility(View.VISIBLE);
            binding.tilSpecificDate.setVisibility(View.GONE);
        } else {
            binding.tilDay.setVisibility(View.GONE);
            binding.tilSpecificDate.setVisibility(View.VISIBLE);
        }
    }

    private void setupClickListeners() {
        binding.toolbar.setNavigationOnClickListener(v -> {
            Navigation.findNavController(v).navigateUp();
        });

        binding.tilStartTime.setEndIconOnClickListener(v -> showTimePicker(true));
        binding.etStartTime.setOnClickListener(v -> showTimePicker(true));

        binding.tilEndTime.setEndIconOnClickListener(v -> showTimePicker(false));
        binding.etEndTime.setOnClickListener(v -> showTimePicker(false));

        // Specific Date Picker
        binding.tilSpecificDate.setEndIconOnClickListener(v -> showDatePicker());
        binding.etSpecificDate.setOnClickListener(v -> showDatePicker());

        binding.btnSave.setOnClickListener(v -> attemptSave());
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog dialog = new DatePickerDialog(
                requireContext(),
                (view, year, month, dayOfMonth) -> {
                    Calendar selectedDate = Calendar.getInstance();
                    selectedDate.set(year, month, dayOfMonth);
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                    selectedSpecificDate = sdf.format(selectedDate.getTime());
                    
                    SimpleDateFormat displayFormat = new SimpleDateFormat("EEE, dd MMM yyyy", Locale.getDefault());
                    binding.etSpecificDate.setText(displayFormat.format(selectedDate.getTime()));
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        dialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
        dialog.show();
    }

    private void showTimePicker(boolean isStartTime) {
        Calendar calendar = Calendar.getInstance();
        TimePickerDialog dialog = new TimePickerDialog(
                requireContext(),
                (view, hourOfDay, minute) -> {
                    String time24 = String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute);
                    SimpleDateFormat inputFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
                    SimpleDateFormat outputFormat = new SimpleDateFormat("hh:mm a", Locale. getDefault());

                    try {
                        String formattedTime = outputFormat. format(inputFormat.parse(time24));
                        if (isStartTime) {
                            selectedStartTime = time24;
                            binding.etStartTime.setText(formattedTime);
                        } else {
                            selectedEndTime = time24;
                            binding.etEndTime.setText(formattedTime);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                false
        );
        dialog.show();
    }

    private void attemptSave() {
        String subject = binding.etSubject.getText().toString().trim();
        String faculty = binding.etFaculty.getText().toString().trim();
        String room = binding.etRoom.getText().toString().trim();
        String dayOfWeek = binding.actvDay.getText().toString().trim();
        String typeDisplay = binding.actvType.getText().toString().trim();

        // Clear errors
        binding.tilSubject.setError(null);
        binding.tilFaculty.setError(null);
        binding.tilRoom.setError(null);
        binding.tilDay.setError(null);
        binding.tilSpecificDate.setError(null);
        binding.tilStartTime.setError(null);
        binding.tilEndTime.setError(null);

        boolean isValid = true;

        if (!ValidationUtils.isNotEmpty(subject)) {
            binding.tilSubject.setError("Subject is required");
            isValid = false;
        }

        if (!ValidationUtils.isNotEmpty(faculty)) {
            binding.tilFaculty.setError("Faculty name is required");
            isValid = false;
        }

        if (!ValidationUtils.isNotEmpty(room)) {
            binding.tilRoom.setError("Room is required");
            isValid = false;
        }

        // Validate based on schedule type
        if (isRecurring) {
            if (!ValidationUtils.isNotEmpty(dayOfWeek)) {
                binding.tilDay.setError("Select a day");
                isValid = false;
            }
        } else {
            if (selectedSpecificDate == null || selectedSpecificDate.isEmpty()) {
                binding.tilSpecificDate.setError("Select a date");
                isValid = false;
            }
        }

        if (selectedStartTime.isEmpty()) {
            binding.tilStartTime.setError("Select start time");
            isValid = false;
        }

        if (selectedEndTime.isEmpty()) {
            binding.tilEndTime.setError("Select end time");
            isValid = false;
        }

        if (!selectedStartTime.isEmpty() && !selectedEndTime.isEmpty()) {
            if (!ValidationUtils.isEndTimeAfterStartTime(selectedStartTime, selectedEndTime)) {
                binding.tilEndTime.setError("End time must be after start time");
                isValid = false;
            }
        }

        if (isValid) {
            String type = convertTypeToValue(typeDisplay);

            if (routineId != null) {
                viewModel.updateRoutine(routineId, subject, faculty, room,
                        dayOfWeek, selectedDayIndex, selectedStartTime, selectedEndTime, type,
                        isRecurring, selectedSpecificDate);
            } else {
                viewModel.createRoutine(subject, faculty, room,
                        dayOfWeek, selectedDayIndex, selectedStartTime, selectedEndTime, type,
                        isRecurring, selectedSpecificDate);
            }
        }
    }

    private String convertTypeToValue(String display) {
        switch (display. toLowerCase()) {
            case "lab":
                return Constants. ROUTINE_TYPE_LAB;
            case "tutorial":
                return Constants.ROUTINE_TYPE_TUTORIAL;
            default:
                return Constants. ROUTINE_TYPE_LECTURE;
        }
    }

    private void observeViewModel() {
        viewModel.getClassroomName().observe(getViewLifecycleOwner(), name -> {
            binding.tvClassroom.setText("Classroom: " + name);
        });

        viewModel.getRoutineData().observe(getViewLifecycleOwner(), routine -> {
            if (routine != null) {
                binding.etSubject.setText(routine.getSubject());
                binding.etFaculty.setText(routine.getFaculty());
                binding.etRoom.setText(routine.getRoom());
                
                // Set schedule type (recurring or specific date)
                isRecurring = routine.isRecurring();
                if (isRecurring) {
                    binding.actvScheduleType.setText("Weekly Recurring", false);
                    binding.actvDay.setText(routine.getDayOfWeek(), false);
                    selectedDayIndex = routine.getDayIndex();
                } else {
                    binding.actvScheduleType.setText("Specific Date", false);
                    selectedSpecificDate = routine.getSpecificDate();
                    if (selectedSpecificDate != null && !selectedSpecificDate.isEmpty()) {
                        try {
                            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                            SimpleDateFormat displayFormat = new SimpleDateFormat("EEE, dd MMM yyyy", Locale.getDefault());
                            binding.etSpecificDate.setText(displayFormat.format(sdf.parse(selectedSpecificDate)));
                        } catch (Exception e) {
                            binding.etSpecificDate.setText(selectedSpecificDate);
                        }
                    }
                }
                updateScheduleTypeVisibility();

                selectedStartTime = routine.getStartTime();
                selectedEndTime = routine.getEndTime();

                try {
                    SimpleDateFormat inputFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
                    SimpleDateFormat outputFormat = new SimpleDateFormat("hh:mm a", Locale.getDefault());
                    binding.etStartTime.setText(outputFormat.format(inputFormat.parse(selectedStartTime)));
                    binding.etEndTime.setText(outputFormat.format(inputFormat.parse(selectedEndTime)));
                } catch (Exception e) {
                    binding.etStartTime.setText(selectedStartTime);
                    binding.etEndTime.setText(selectedEndTime);
                }

                // Set type
                String typeDisplay = "Lecture";
                if (routine.getType().equalsIgnoreCase(Constants.ROUTINE_TYPE_LAB)) {
                    typeDisplay = "Lab";
                } else if (routine.getType().equalsIgnoreCase(Constants.ROUTINE_TYPE_TUTORIAL)) {
                    typeDisplay = "Tutorial";
                }
                binding.actvType.setText(typeDisplay, false);
            }
        });

        viewModel.getSaveResult().observe(getViewLifecycleOwner(), resource -> {
            switch (resource.status) {
                case LOADING:
                    showLoading(true);
                    break;

                case SUCCESS:
                    showLoading(false);
                    Toast.makeText(requireContext(),
                            routineId != null ? R.string.routine_updated : R.string.routine_added,
                            Toast.LENGTH_SHORT).show();
                    Navigation.findNavController(requireView()).navigateUp();
                    break;

                case ERROR:
                    showLoading(false);
                    Toast.makeText(requireContext(), resource.message, Toast.LENGTH_LONG).show();
                    break;
            }
        });
    }

    private void showLoading(boolean isLoading) {
        binding.progressBar.setVisibility(isLoading ? View.VISIBLE :  View.GONE);
        binding.btnSave.setEnabled(! isLoading);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
