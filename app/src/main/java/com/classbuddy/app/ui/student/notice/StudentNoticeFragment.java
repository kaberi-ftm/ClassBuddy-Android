package com.classbuddy.app.ui.student.notice;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.classbuddy.app.R;
import com.classbuddy.app.adapter.NoticeAdapter;
import com.classbuddy.app.data.model.Notice;
import com.classbuddy.app.databinding.FragmentStudentNoticeBinding;
import com.classbuddy.app.util.DateTimeUtils;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class StudentNoticeFragment extends Fragment {

    private FragmentStudentNoticeBinding binding;
    private StudentNoticeViewModel viewModel;
    private NoticeAdapter adapter;

    private String classroomId = null;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentStudentNoticeBinding. inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getArguments() != null) {
            classroomId = getArguments().getString("classroomId");
        }

        viewModel = new ViewModelProvider(this).get(StudentNoticeViewModel.class);

        setupRecyclerView();
        setupSearch();
        setupClickListeners();
        observeViewModel();

        if (classroomId != null) {
            viewModel.loadNoticesForClassroom(classroomId);
        } else {
            viewModel.loadAllNotices();
        }
    }

    private void setupRecyclerView() {
        adapter = new NoticeAdapter((notice, position) -> {
            showNoticeDetail(notice);
            viewModel.markAsRead(notice.getId());
        });

        binding.rvNotices.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding. rvNotices.setAdapter(adapter);
    }

    private void setupSearch() {
        binding.searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                viewModel.searchNotices(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                viewModel.searchNotices(newText);
                return true;
            }
        });
    }

    private void setupClickListeners() {
        binding.toolbar.setNavigationOnClickListener(v -> {
            requireActivity().onBackPressed();
        });

        binding.swipeRefresh. setOnRefreshListener(() -> {
            if (classroomId != null) {
                viewModel. loadNoticesForClassroom(classroomId);
            } else {
                viewModel.loadAllNotices();
            }
        });

        // Filter chips
        binding.chipAll.setOnClickListener(v -> viewModel.filterByPriority(null));
        binding.chipUrgent.setOnClickListener(v -> viewModel.filterByPriority("urgent"));
        binding.chipHigh.setOnClickListener(v -> viewModel.filterByPriority("high"));
        binding.chipNormal.setOnClickListener(v -> viewModel.filterByPriority("normal"));
    }

    private void showNoticeDetail(Notice notice) {
        BottomSheetDialog bottomSheet = new BottomSheetDialog(requireContext());
        View sheetView = getLayoutInflater().inflate(R.layout. bottom_sheet_notice_detail, null);
        bottomSheet.setContentView(sheetView);

        // Bind data
        android.widget.TextView tvTitle = sheetView.findViewById(R. id.tvTitle);
        android.widget.TextView tvContent = sheetView. findViewById(R.id.tvContent);
        android.widget. TextView tvClassroom = sheetView.findViewById(R. id.tvClassroom);
        android.widget.TextView tvAuthor = sheetView.findViewById(R. id.tvAuthor);
        android.widget.TextView tvTime = sheetView. findViewById(R.id.tvTime);
        android.widget. ImageView ivImage = sheetView. findViewById(R.id.ivImage);
        com.google.android.material. button.MaterialButton btnShare = sheetView. findViewById(R.id.btnShare);

        tvTitle.setText(notice.getTitle());
        tvContent.setText(notice. getContent());
        tvClassroom.setText(notice.getClassroomName());
        tvAuthor.setText("Posted by " + notice. getAdminName());
        tvTime.setText(DateTimeUtils.formatDateTime(notice.getCreatedAt()));

        if (notice.getImageUrl() != null && ! notice.getImageUrl().isEmpty()) {
            ivImage.setVisibility(View. VISIBLE);
            Glide.with(this)
                    . load(notice.getImageUrl())
                    .centerCrop()
                    .into(ivImage);
        } else {
            ivImage.setVisibility(View.GONE);
        }

        btnShare.setOnClickListener(v -> {
            shareNotice(notice);
            bottomSheet.dismiss();
        });

        bottomSheet.show();
    }

    private void shareNotice(Notice notice) {
        String shareText = notice.getTitle() + "\n\n" +
                notice.getContent() + "\n\n" +
                "From: " + notice. getClassroomName() + "\n" +
                "Posted by: " + notice. getAdminName();

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent. putExtra(Intent. EXTRA_SUBJECT, notice.getTitle());
        shareIntent.putExtra(Intent. EXTRA_TEXT, shareText);
        startActivity(Intent.createChooser(shareIntent, "Share Notice"));
    }

    private void observeViewModel() {
        viewModel.getFilteredNotices().observe(getViewLifecycleOwner(), resource -> {
            binding.swipeRefresh.setRefreshing(false);

            switch (resource. status) {
                case LOADING:
                    binding.progressBar.setVisibility(View.VISIBLE);
                    binding.layoutEmpty.setVisibility(View.GONE);
                    break;

                case SUCCESS:
                    binding.progressBar.setVisibility(View.GONE);
                    if (resource.data != null && ! resource.data.isEmpty()) {
                        binding. rvNotices.setVisibility(View. VISIBLE);
                        binding.layoutEmpty.setVisibility(View.GONE);
                        adapter. submitList(resource. data);
                    } else {
                        binding.rvNotices.setVisibility(View. GONE);
                        binding.layoutEmpty. setVisibility(View.VISIBLE);
                    }
                    break;

                case ERROR:
                    binding.progressBar.setVisibility(View.GONE);
                    binding.layoutEmpty.setVisibility(View. VISIBLE);
                    break;
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
