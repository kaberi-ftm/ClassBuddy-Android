package com.classbuddy.app.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.classbuddy.app.R;
import com.classbuddy.app.data.model.Routine;
import com.classbuddy.app.databinding.ItemRoutineBinding;
import com.classbuddy.app.util.Constants;
import com.classbuddy.app.util.DateTimeUtils;

public class RoutineAdapter extends ListAdapter<Routine, RoutineAdapter.RoutineViewHolder> {

    private final OnRoutineClickListener listener;

    public interface OnRoutineClickListener {
        void onRoutineClick(Routine routine);
    }

    public RoutineAdapter(OnRoutineClickListener listener) {
        super(DIFF_CALLBACK);
        this.listener = listener;
    }

    private static final DiffUtil.ItemCallback<Routine> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<Routine>() {
                @Override
                public boolean areItemsTheSame(@NonNull Routine oldItem, @NonNull Routine newItem) {
                    return oldItem. getId().equals(newItem.getId());
                }

                @Override
                public boolean areContentsTheSame(@NonNull Routine oldItem, @NonNull Routine newItem) {
                    return oldItem.getSubject().equals(newItem.getSubject()) &&
                            oldItem.getStartTime().equals(newItem.getStartTime());
                }
            };

    @NonNull
    @Override
    public RoutineViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemRoutineBinding binding = ItemRoutineBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new RoutineViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull RoutineViewHolder holder, int position) {
        holder.bind(getItem(position));
    }

    class RoutineViewHolder extends RecyclerView.ViewHolder {
        private final ItemRoutineBinding binding;

        RoutineViewHolder(ItemRoutineBinding binding) {
            super(binding. getRoot());
            this.binding = binding;
        }

        void bind(Routine routine) {
            binding.tvSubject.setText(routine.getSubject());
            binding.tvFaculty.setText(routine.getFaculty());
            binding.tvRoom.setText("Room " + routine.getRoom());
            binding.tvClassroomName.setText(routine.getClassroomName());

            binding.tvStartTime.setText(DateTimeUtils.formatTime(routine.getStartTime()));
            binding.tvEndTime.setText(DateTimeUtils.formatTime(routine.getEndTime()));

            // Set type
            String typeDisplay = routine.getType().toUpperCase();
            binding.tvType.setText(typeDisplay);

            // Handle cancelled status
            if (routine.isCancelled()) {
                binding.getRoot().setAlpha(0.6f);
                binding.tvSubject.setText(routine.getSubject() + " (CANCELLED)");
                // Use error color for cancelled
                binding.timeContainer.setBackgroundResource(R.color.error);
            } else {
                binding.getRoot().setAlpha(1.0f);
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
            }

            binding.getRoot().setOnClickListener(v -> {
                if (listener != null) {
                    listener.onRoutineClick(routine);
                }
            });
        }
    }
}
