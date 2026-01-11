package com.classbuddy.app.ui.student.exam;

import android.content.Intent;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.classbuddy.app.R;
import com.classbuddy.app.adapter.ExamAdapter;
import com.classbuddy.app.data.model.Exam;
import com.classbuddy.app.databinding.FragmentStudentExamBinding;
import com.classbuddy.app.util.DateTimeUtils;
import com.classbuddy.app.util.NotificationUtils;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.Calendar;

public class StudentExamFragment extends Fragment {

    private FragmentStudentExamBinding binding;
    private StudentExamViewModel viewModel;
    private ExamAdapter upcomingAdapter;
    private ExamAdapter pastAdapter;

    private String classroomId = null;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentStudentExamBinding.inflate(inflater, container, false);
        return binding. getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getArguments() != null) {
            classroomId = getArguments().getString("classroomId");
        }

        viewModel = new ViewModelProvider(this).get(StudentExamViewModel.class);

        setupRecyclerViews();
        setupClickListeners();
        observeViewModel();

        if (classroomId != null) {
            viewModel.loadExamsForClassroom(classroomId);
        } else {
            viewModel.loadAllExams();
        }
    }

    private void setupRecyclerViews() {
        // Upcoming Exams
        upcomingAdapter = new ExamAdapter(this:: showExamOptions);
        binding. rvUpcomingExams.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding. rvUpcomingExams.setAdapter(upcomingAdapter);
        binding.rvUpcomingExams.setNestedScrollingEnabled(false);

        // Past Exams
        pastAdapter = new ExamAdapter(exam -> {
            // Show past exam details
            showExamDetails(exam);
        });
        binding.rvPastExams.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvPastExams. setAdapter(pastAdapter);
        binding.rvPastExams.setNestedScrollingEnabled(false);
    }

    private void setupClickListeners() {
        binding.toolbar. setNavigationOnClickListener(v -> {
            requireActivity().onBackPressed();
        });

        binding.swipeRefresh.setOnRefreshListener(() -> {
            if (classroomId != null) {
                viewModel.loadExamsForClassroom(classroomId);
            } else {
                viewModel.loadAllExams();
            }
        });

        // Toggle sections
        binding.headerUpcoming.setOnClickListener(v -> {
            toggleSection(binding. rvUpcomingExams, binding.ivUpcomingArrow);
        });

        binding.headerPast.setOnClickListener(v -> {
            toggleSection(binding.rvPastExams, binding.ivPastArrow);
        });
    }

    private void toggleSection(View recyclerView, View arrow) {
        if (recyclerView.getVisibility() == View.VISIBLE) {
            recyclerView.setVisibility(View. GONE);
            arrow.setRotation(0);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            arrow. setRotation(180);
        }
    }

    private void showExamOptions(Exam exam) {
        String[] options = {"View Details", "Set Reminder", "Add to Calendar"};

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(exam.getCourseName())
                .setItems(options, (dialog, which) -> {
                    switch (which) {
                        case 0:
                            showExamDetails(exam);
                            break;
                        case 1:
                            setExamReminder(exam);
                            break;
                        case 2:
                            addToCalendar(exam);
                            break;
                    }
                })
                .show();
    }

    private void showExamDetails(Exam exam) {
        String message = "Course: " + exam. getCourseNo() + " - " + exam.getCourseName() + "\n" +
                "Type: " + exam. getExamTypeDisplay() + "\n" +
                "Date: " + DateTimeUtils.formatDate(exam.getExamDate()) + "\n" +
                "Time: " + DateTimeUtils.formatTime(exam.getStartTime());

        if (exam.getEndTime() != null && ! exam.getEndTime().isEmpty()) {
            message += " - " + DateTimeUtils.formatTime(exam.getEndTime());
        }

        if (exam.getRoom() != null && !exam.getRoom().isEmpty()) {
            message += "\nRoom: " + exam.getRoom();
        }

        message += "\nTotal Marks: " + exam. getTotalMarks();

        if (exam.getNotes() != null && !exam.getNotes().isEmpty()) {
            message += "\n\nNotes:  " + exam.getNotes();
        }

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(exam.getExamTypeDisplay() + " - " + exam.getCourseName())
                .setMessage(message)
                .setPositiveButton(R.string.ok, null)
                .show();
    }

    private void setExamReminder(Exam exam) {
        // Schedule reminders
        NotificationUtils.scheduleExamReminder(
                requireContext(),
                exam,
                com.classbuddy. app.util.Constants.REMINDER_24_HOURS
        );
        NotificationUtils.scheduleExamReminder(
                requireContext(),
                exam,
                com.classbuddy.app.util.Constants. REMINDER_1_HOUR
        );

        Toast.makeText(requireContext(), R.string.reminder_set, Toast. LENGTH_SHORT).show();
    }

    private void addToCalendar(Exam exam) {
        Calendar beginTime = Calendar.getInstance();
        beginTime. setTime(exam. getExamDate().toDate());

        // Parse start time
        String[] timeParts = exam. getStartTime().split(":");
        if (timeParts. length >= 2) {
            beginTime.set(Calendar.HOUR_OF_DAY, Integer.parseInt(timeParts[0]));
            beginTime.set(Calendar.MINUTE, Integer.parseInt(timeParts[1]));
        }

        Calendar endTime = Calendar. getInstance();
        endTime.setTimeInMillis(beginTime.getTimeInMillis());
        endTime.add(Calendar.HOUR_OF_DAY, 2); // Default 2 hour duration

        Intent intent = new Intent(Intent.ACTION_INSERT)
                .setData(CalendarContract. Events.CONTENT_URI)
                .putExtra(CalendarContract. EXTRA_EVENT_BEGIN_TIME, beginTime.getTimeInMillis())
                .putExtra(CalendarContract. EXTRA_EVENT_END_TIME, endTime.getTimeInMillis())
                .putExtra(CalendarContract.Events.TITLE, exam.getExamTypeDisplay() + ": " + exam. getCourseName())
                .putExtra(CalendarContract. Events.DESCRIPTION, "Course: " + exam. getCourseNo() + "\nMarks: " + exam.getTotalMarks())
                .putExtra(CalendarContract.Events.EVENT_LOCATION, exam.getRoom());

        if (intent.resolveActivity(requireActivity().getPackageManager()) != null) {
            startActivity(intent);
        } else {
            Toast.makeText(requireContext(), "No calendar app found", Toast. LENGTH_SHORT).show();
        }
    }

    private void observeViewModel() {
        viewModel.getUpcomingExams().observe(getViewLifecycleOwner(), resource -> {
            binding.swipeRefresh.setRefreshing(false);

            if (resource.isSuccess() && resource.data != null) {
                if (resource.data. isEmpty()) {
                    binding.layoutUpcoming.setVisibility(View.GONE);
                } else {
                    binding. layoutUpcoming.setVisibility(View. VISIBLE);
                    binding.tvUpcomingCount.setText("(" + resource.data. size() + ")");
                    upcomingAdapter.submitList(resource.data);
                }
            }
        });

        viewModel.getPastExams().observe(getViewLifecycleOwner(), resource -> {
            if (resource.isSuccess() && resource.data != null) {
                if (resource. data.isEmpty()) {
                    binding. layoutPast.setVisibility(View. GONE);
                } else {
                    binding.layoutPast.setVisibility(View.VISIBLE);
                    binding.tvPastCount.setText("(" + resource.data.size() + ")");
                    pastAdapter. submitList(resource. data);
                }
            }
        });

        viewModel.getIsEmpty().observe(getViewLifecycleOwner(), isEmpty -> {
            binding.layoutEmpty.setVisibility(isEmpty ?  View.VISIBLE :  View.GONE);
        });
    }

    @Override
    public void onDestroyView() {
        super. onDestroyView();
        binding = null;
    }
}
