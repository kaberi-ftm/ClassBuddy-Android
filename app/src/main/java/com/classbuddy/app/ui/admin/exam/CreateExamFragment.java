package com.classbuddy.app.ui.admin.exam;

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
import com.classbuddy.app.data.model.Classroom;
import com.classbuddy.app.databinding.FragmentCreateExamBinding;
import com.classbuddy.app.util.Constants;
import com.classbuddy.app.util.ValidationUtils;
import com.google.firebase.Timestamp;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class CreateExamFragment extends Fragment {

    private FragmentCreateExamBinding binding;
    private CreateExamViewModel viewModel;

    private Calendar selectedDate = Calendar.getInstance();
    private String selectedStartTime = "";
    private String selectedEndTime = "";
    private String selectedClassroomId = "";
    private String selectedClassroomName = "";
    private List<Classroom> classrooms = new ArrayList<>();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentCreateExamBinding. inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(CreateExamViewModel.class);

        setupViews();
        setupClickListeners();
        observeViewModel();
    }

    private void setupViews() {
        // Exam Type Dropdown
        String[] examTypes = {"CT", "Final", "Lab Quiz", "Viva"};
        ArrayAdapter<String> examTypeAdapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout. simple_dropdown_item_1line,
                examTypes
        );
        binding.actvExamType.setAdapter(examTypeAdapter);
    }

    private void setupClickListeners() {
        binding. toolbar.setNavigationOnClickListener(v -> {
            Navigation.findNavController(v).navigateUp();
        });

        binding.tilDate.setEndIconOnClickListener(v -> showDatePicker());
        binding.etDate.setOnClickListener(v -> showDatePicker());

        binding.tilStartTime.setEndIconOnClickListener(v -> showTimePicker(true));
        binding.etStartTime.setOnClickListener(v -> showTimePicker(true));

        binding.tilEndTime.setEndIconOnClickListener(v -> showTimePicker(false));
        binding.etEndTime.setOnClickListener(v -> showTimePicker(false));

        binding.btnCreate.setOnClickListener(v -> attemptCreate());
    }

    private void showDatePicker() {
        DatePickerDialog dialog = new DatePickerDialog(
                requireContext(),
                (view, year, month, dayOfMonth) -> {
                    selectedDate.set(year, month, dayOfMonth);
                    SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
                    binding.etDate.setText(sdf.format(selectedDate.getTime()));
                },
                selectedDate. get(Calendar.YEAR),
                selectedDate.get(Calendar. MONTH),
                selectedDate.get(Calendar.DAY_OF_MONTH)
        );
        dialog.getDatePicker().setMinDate(System.currentTimeMillis());
        dialog.show();
    }

    private void showTimePicker(boolean isStartTime) {
        Calendar calendar = Calendar.getInstance();
        TimePickerDialog dialog = new TimePickerDialog(
                requireContext(),
                (view, hourOfDay, minute) -> {
                    String time = String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute);
                    SimpleDateFormat inputFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
                    SimpleDateFormat outputFormat = new SimpleDateFormat("hh:mm a", Locale.getDefault());

                    try {
                        String formattedTime = outputFormat.format(inputFormat.parse(time));
                        if (isStartTime) {
                            selectedStartTime = time;
                            binding.etStartTime.setText(formattedTime);
                        } else {
                            selectedEndTime = time;
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

    private void attemptCreate() {
        String courseNo = binding.etCourseNo.getText().toString().trim();
        String courseName = binding.etCourseName.getText().toString().trim();
        String examTypeDisplay = binding.actvExamType.getText().toString().trim();
        String room = binding.etRoom.getText().toString().trim();
        String marksStr = binding.etMarks.getText().toString().trim();
        String notes = binding.etNotes.getText().toString().trim();

        // Clear errors
        binding.tilCourseNo. setError(null);
        binding.tilCourseName.setError(null);
        binding.tilExamType. setError(null);
        binding.tilClassroom.setError(null);
        binding.tilDate.setError(null);
        binding.tilStartTime.setError(null);
        binding.tilMarks.setError(null);

        boolean isValid = true;

        if (! ValidationUtils.isNotEmpty(courseNo)) {
            binding.tilCourseNo. setError("Course No.  is required");
            isValid = false;
        }

        if (!ValidationUtils. isNotEmpty(courseName)) {
            binding.tilCourseName.setError("Course name is required");
            isValid = false;
        }

        if (!ValidationUtils. isNotEmpty(examTypeDisplay)) {
            binding.tilExamType.setError("Select exam type");
            isValid = false;
        }

        if (selectedClassroomId.isEmpty()) {
            binding.tilClassroom. setError("Select a classroom");
            isValid = false;
        }

        if (binding.etDate.getText().toString().isEmpty()) {
            binding.tilDate.setError("Select date");
            isValid = false;
        }

        if (selectedStartTime.isEmpty()) {
            binding. tilStartTime.setError("Select start time");
            isValid = false;
        }

        int marks = 0;
        if (!marksStr. isEmpty()) {
            try {
                marks = Integer.parseInt(marksStr);
                if (marks <= 0 || marks > 100) {
                    binding.tilMarks.setError("Marks should be between 1-100");
                    isValid = false;
                }
            } catch (NumberFormatException e) {
                binding.tilMarks.setError("Invalid marks");
                isValid = false;
            }
        }

        if (isValid) {
            // Convert exam type display to value
            String examType = convertExamTypeToValue(examTypeDisplay);

            Timestamp examDate = new Timestamp(selectedDate.getTime());

            viewModel.createExam(
                    selectedClassroomId,
                    selectedClassroomName,
                    courseNo,
                    courseName,
                    examType,
                    examDate,
                    selectedStartTime,
                    selectedEndTime,
                    room,
                    marks,
                    notes
            );
        }
    }

    private String convertExamTypeToValue(String display) {
        switch (display. toLowerCase()) {
            case "ct":  return Constants.EXAM_TYPE_CT;
            case "final": return Constants.EXAM_TYPE_FINAL;
            case "lab quiz": return Constants. EXAM_TYPE_LAB_QUIZ;
            case "viva": return Constants.EXAM_TYPE_VIVA;
            default: return Constants.EXAM_TYPE_CT;
        }
    }

    private void observeViewModel() {
        viewModel.getClassrooms().observe(getViewLifecycleOwner(), resource -> {
            if (resource.isSuccess() && resource.data != null) {
                classrooms = resource.data;
                setupClassroomDropdown();
            }
        });

        viewModel.getCreateResult().observe(getViewLifecycleOwner(), resource -> {
            switch (resource.status) {
                case LOADING:
                    showLoading(true);
                    break;

                case SUCCESS:
                    showLoading(false);
                    Toast.makeText(requireContext(), R.string.exam_created, Toast.LENGTH_SHORT).show();
                    Navigation.findNavController(requireView()).navigateUp();
                    break;

                case ERROR:
                    showLoading(false);
                    Toast.makeText(requireContext(), resource.message, Toast.LENGTH_LONG).show();
                    break;
            }
        });
    }

    private void setupClassroomDropdown() {
        List<String> classroomNames = new ArrayList<>();
        for (Classroom classroom : classrooms) {
            classroomNames.add(classroom.getName());
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(),
                android. R.layout.simple_dropdown_item_1line,
                classroomNames
        );
        binding.actvClassroom.setAdapter(adapter);

        binding.actvClassroom.setOnItemClickListener((parent, view, position, id) -> {
            Classroom selected = classrooms.get(position);
            selectedClassroomId = selected.getId();
            selectedClassroomName = selected.getName();
        });
    }

    private void showLoading(boolean isLoading) {
        binding.progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        binding.btnCreate.setEnabled(! isLoading);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
