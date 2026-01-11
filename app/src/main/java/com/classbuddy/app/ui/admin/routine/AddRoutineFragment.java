package com.classbuddy.app.ui.admin.routine;

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
    }

    private void setupClickListeners() {
        binding.toolbar.setNavigationOnClickListener(v -> {
            Navigation.findNavController(v).navigateUp();
        });

        binding.tilStartTime.setEndIconOnClickListener(v -> showTimePicker(true));
        binding.etStartTime.setOnClickListener(v -> showTimePicker(true));

        binding.tilEndTime.setEndIconOnClickListener(v -> showTimePicker(false));
        binding.etEndTime.setOnClickListener(v -> showTimePicker(false));

        binding.btnSave.setOnClickListener(v -> attemptSave());
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
        binding.tilSubject. setError(null);
        binding.tilFaculty.setError(null);
        binding.tilRoom.setError(null);
        binding.tilDay.setError(null);
        binding.tilStartTime.setError(null);
        binding.tilEndTime.setError(null);

        boolean isValid = true;

        if (! ValidationUtils.isNotEmpty(subject)) {
            binding. tilSubject.setError("Subject is required");
            isValid = false;
        }

        if (!ValidationUtils.isNotEmpty(faculty)) {
            binding. tilFaculty.setError("Faculty name is required");
            isValid = false;
        }

        if (!ValidationUtils.isNotEmpty(room)) {
            binding.tilRoom.setError("Room is required");
            isValid = false;
        }

        if (! ValidationUtils.isNotEmpty(dayOfWeek)) {
            binding.tilDay.setError("Select a day");
            isValid = false;
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
            if (! ValidationUtils.isEndTimeAfterStartTime(selectedStartTime, selectedEndTime)) {
                binding.tilEndTime.setError("End time must be after start time");
                isValid = false;
            }
        }

        if (isValid) {
            String type = convertTypeToValue(typeDisplay);

            if (routineId != null) {
                viewModel. updateRoutine(routineId, subject, faculty, room,
                        dayOfWeek, selectedDayIndex, selectedStartTime, selectedEndTime, type);
            } else {
                viewModel.createRoutine(subject, faculty, room,
                        dayOfWeek, selectedDayIndex, selectedStartTime, selectedEndTime, type);
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
                binding.etFaculty.setText(routine. getFaculty());
                binding. etRoom.setText(routine.getRoom());
                binding.actvDay.setText(routine.getDayOfWeek(), false);
                selectedDayIndex = routine.getDayIndex();

                selectedStartTime = routine.getStartTime();
                selectedEndTime = routine.getEndTime();

                try {
                    SimpleDateFormat inputFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
                    SimpleDateFormat outputFormat = new SimpleDateFormat("hh:mm a", Locale.getDefault());
                    binding.etStartTime.setText(outputFormat.format(inputFormat.parse(selectedStartTime)));
                    binding.etEndTime.setText(outputFormat.format(inputFormat. parse(selectedEndTime)));
                } catch (Exception e) {
                    binding.etStartTime.setText(selectedStartTime);
                    binding.etEndTime.setText(selectedEndTime);
                }

                // Set type
                String typeDisplay = "Lecture";
                if (routine.getType().equalsIgnoreCase(Constants. ROUTINE_TYPE_LAB)) {
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
