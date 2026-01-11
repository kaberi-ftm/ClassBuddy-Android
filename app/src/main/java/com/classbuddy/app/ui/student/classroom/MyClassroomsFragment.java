package com.classbuddy.app.ui.student.classroom;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.classbuddy.app.R;
import com.classbuddy.app.adapter.ClassroomAdapter;
import com.classbuddy.app.databinding.FragmentMyClassroomsBinding;

public class MyClassroomsFragment extends Fragment {

    private FragmentMyClassroomsBinding binding;
    private MyClassroomsViewModel viewModel;
    private ClassroomAdapter adapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentMyClassroomsBinding. inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(MyClassroomsViewModel. class);

        setupRecyclerView();
        setupClickListeners();
        observeViewModel();
    }

    private void setupRecyclerView() {
        adapter = new ClassroomAdapter(classroom -> {
            Bundle bundle = new Bundle();
            bundle.putString("classroomId", classroom.getId());
            Navigation.findNavController(requireView())
                    .navigate(R.id. action_myClassrooms_to_classroomDetail, bundle);
        });

        binding.rvClassrooms.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding. rvClassrooms.setAdapter(adapter);
    }

    private void setupClickListeners() {
        binding.fabJoinClassroom.setOnClickListener(v -> {
            Navigation.findNavController(v)
                    .navigate(R.id. action_myClassrooms_to_joinClassroom);
        });

        binding.swipeRefresh.setOnRefreshListener(() -> {
            viewModel.refreshClassrooms();
        });

        binding.btnJoinFirst.setOnClickListener(v -> {
            Navigation.findNavController(v)
                    .navigate(R.id.action_myClassrooms_to_joinClassroom);
        });
    }

    private void observeViewModel() {
        viewModel.getClassrooms().observe(getViewLifecycleOwner(), resource -> {
            binding.swipeRefresh.setRefreshing(false);

            switch (resource. status) {
                case LOADING:
                    binding.progressBar.setVisibility(View.VISIBLE);
                    binding.layoutEmpty.setVisibility(View.GONE);
                    break;

                case SUCCESS:
                    binding.progressBar.setVisibility(View.GONE);
                    if (resource.data != null && ! resource.data.isEmpty()) {
                        binding. rvClassrooms.setVisibility(View. VISIBLE);
                        binding.layoutEmpty.setVisibility(View. GONE);
                        adapter.submitList(resource.data);
                    } else {
                        binding.rvClassrooms.setVisibility(View.GONE);
                        binding.layoutEmpty.setVisibility(View. VISIBLE);
                    }
                    break;

                case ERROR:
                    binding.progressBar.setVisibility(View. GONE);
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
