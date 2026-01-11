package com.classbuddy.app.ui.admin.notice;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.classbuddy.app.R;
import com.classbuddy.app.adapter.NoticeAdapter;
import com.classbuddy.app.data.model.Notice;
import com.classbuddy.app.databinding.FragmentAdminNoticeBinding;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class AdminNoticeFragment extends Fragment {

    private FragmentAdminNoticeBinding binding;
    private AdminNoticeViewModel viewModel;
    private NoticeAdapter adapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentAdminNoticeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(AdminNoticeViewModel.class);

        setupRecyclerView();
        setupClickListeners();
        observeViewModel();
    }

    private void setupRecyclerView() {
        adapter = new NoticeAdapter((notice, position) -> {
            showNoticeOptions(notice);
        });

        binding.rvNotices.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvNotices. setAdapter(adapter);
    }

    private void setupClickListeners() {
        binding.fabCreateNotice.setOnClickListener(v -> {
            Navigation.findNavController(v)
                    .navigate(R.id.action_notices_to_createNotice);
        });

        binding.swipeRefresh.setOnRefreshListener(() -> viewModel.refreshNotices());

        binding.btnCreateFirst.setOnClickListener(v -> {
            Navigation.findNavController(v)
                    .navigate(R. id.action_notices_to_createNotice);
        });
    }

    private void showNoticeOptions(Notice notice) {
        String[] options = {
                notice.isPinned() ? "Unpin Notice" : "Pin Notice",
                "Edit Notice",
                "Delete Notice"
        };

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(notice.getTitle())
                .setItems(options, (dialog, which) -> {
                    switch (which) {
                        case 0:
                            viewModel.togglePin(notice.getId(), ! notice.isPinned());
                            break;
                        case 1:
                            Bundle bundle = new Bundle();
                            bundle.putString("noticeId", notice. getId());
                            Navigation.findNavController(requireView())
                                    . navigate(R.id.action_notices_to_createNotice, bundle);
                            break;
                        case 2:
                            confirmDelete(notice);
                            break;
                    }
                })
                .show();
    }

    private void confirmDelete(Notice notice) {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.delete)
                .setMessage("Are you sure you want to delete this notice? ")
                .setPositiveButton(R.string.delete, (dialog, which) -> {
                    viewModel.deleteNotice(notice. getId());
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void observeViewModel() {
        viewModel.getNotices().observe(getViewLifecycleOwner(), resource -> {
            binding.swipeRefresh.setRefreshing(false);

            switch (resource. status) {
                case LOADING:
                    binding.progressBar.setVisibility(View. VISIBLE);
                    binding.layoutEmpty.setVisibility(View.GONE);
                    break;

                case SUCCESS:
                    binding.progressBar.setVisibility(View. GONE);
                    if (resource.data != null && ! resource.data.isEmpty()) {
                        binding. rvNotices.setVisibility(View. VISIBLE);
                        binding.layoutEmpty.setVisibility(View. GONE);
                        adapter.submitList(resource.data);
                    } else {
                        binding.rvNotices.setVisibility(View.GONE);
                        binding.layoutEmpty.setVisibility(View. VISIBLE);
                    }
                    break;

                case ERROR:
                    binding.progressBar. setVisibility(View.GONE);
                    binding.layoutEmpty.setVisibility(View.VISIBLE);
                    Toast.makeText(requireContext(), resource.message, Toast.LENGTH_SHORT).show();
                    break;
            }
        });

        viewModel. getActionResult().observe(getViewLifecycleOwner(), resource -> {
            if (resource.isSuccess()) {
                Toast.makeText(requireContext(), "Action completed", Toast.LENGTH_SHORT).show();
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
