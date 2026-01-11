package com.classbuddy.app.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.classbuddy.app.R;
import com.classbuddy.app.data.model.Routine;
import com.classbuddy.app.databinding.ItemRoutineBinding;
import com.classbuddy.app.databinding.ItemRoutineHeaderBinding;
import com.classbuddy.app.util.Constants;
import com.classbuddy.app.util.DateTimeUtils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Adapter that displays routines grouped by classroom with section headers.
 * Provides better organization when viewing routines from multiple classrooms.
 */
public class GroupedRoutineAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_HEADER = 0;
    private static final int VIEW_TYPE_ROUTINE = 1;

    private final OnRoutineClickListener listener;
    private final List<Object> items = new ArrayList<>();

    public interface OnRoutineClickListener {
        void onRoutineClick(Routine routine);
    }

    public GroupedRoutineAdapter(OnRoutineClickListener listener) {
        this.listener = listener;
    }

    public void submitList(List<Routine> routines) {
        items.clear();

        if (routines == null || routines.isEmpty()) {
            notifyDataSetChanged();
            return;
        }

        // Group routines by classroom
        Map<String, List<Routine>> grouped = new LinkedHashMap<>();
        for (Routine routine : routines) {
            String classroomName = routine.getClassroomName();
            if (!grouped.containsKey(classroomName)) {
                grouped.put(classroomName, new ArrayList<>());
            }
            grouped.get(classroomName).add(routine);
        }

        // Build flat list with headers
        for (Map.Entry<String, List<Routine>> entry : grouped.entrySet()) {
            // Add header
            items.add(new ClassroomHeader(entry.getKey(), entry.getValue().size()));

            // Add routines sorted by time
            List<Routine> classroomRoutines = entry.getValue();
            classroomRoutines.sort((r1, r2) -> r1.getStartTime().compareTo(r2.getStartTime()));
            items.addAll(classroomRoutines);
        }

        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        return items.get(position) instanceof ClassroomHeader ? VIEW_TYPE_HEADER : VIEW_TYPE_ROUTINE;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        if (viewType == VIEW_TYPE_HEADER) {
            ItemRoutineHeaderBinding binding = ItemRoutineHeaderBinding.inflate(inflater, parent, false);
            return new HeaderViewHolder(binding);
        } else {
            ItemRoutineBinding binding = ItemRoutineBinding.inflate(inflater, parent, false);
            return new RoutineViewHolder(binding);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof HeaderViewHolder) {
            ((HeaderViewHolder) holder).bind((ClassroomHeader) items.get(position));
        } else if (holder instanceof RoutineViewHolder) {
            ((RoutineViewHolder) holder).bind((Routine) items.get(position));
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    // Header data class
    private static class ClassroomHeader {
        final String classroomName;
        final int routineCount;

        ClassroomHeader(String classroomName, int routineCount) {
            this.classroomName = classroomName;
            this.routineCount = routineCount;
        }
    }

    // Header ViewHolder
    class HeaderViewHolder extends RecyclerView.ViewHolder {
        private final ItemRoutineHeaderBinding binding;

        HeaderViewHolder(ItemRoutineHeaderBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(ClassroomHeader header) {
            binding.tvClassroomName.setText(header.classroomName);
            binding.tvClassCount.setText(header.routineCount + (header.routineCount == 1 ? " class" : " classes"));
        }
    }

    // Routine ViewHolder
    class RoutineViewHolder extends RecyclerView.ViewHolder {
        private final ItemRoutineBinding binding;

        RoutineViewHolder(ItemRoutineBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(Routine routine) {
            binding.tvSubject.setText(routine.getSubject());
            binding.tvFaculty.setText(routine.getFaculty());
            binding.tvRoom.setText("Room " + routine.getRoom());

            // Hide classroom name in grouped view since it's shown in header
            binding.tvClassroomName.setVisibility(View.GONE);

            binding.tvStartTime.setText(DateTimeUtils.formatTime(routine.getStartTime()));
            binding.tvEndTime.setText(DateTimeUtils.formatTime(routine.getEndTime()));

            // Set type
            String typeDisplay = routine.getType().toUpperCase();
            binding.tvType.setText(typeDisplay);

            // Set time container color based on type
            int color;
            switch (routine.getType().toLowerCase()) {
                case Constants.ROUTINE_TYPE_LAB:
                    color = R.color.event_lab;
                    break;
                case Constants.ROUTINE_TYPE_TUTORIAL:
                    color = R.color.secondary;
                    break;
                default:
                    color = R.color.primary;
            }
            binding.timeContainer.setBackgroundResource(color);

            binding.getRoot().setOnClickListener(v -> {
                if (listener != null) {
                    listener.onRoutineClick(routine);
                }
            });
        }
    }
}
