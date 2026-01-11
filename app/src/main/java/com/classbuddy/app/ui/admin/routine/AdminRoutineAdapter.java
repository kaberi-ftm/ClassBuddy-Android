package com.classbuddy.app.ui.admin.routine;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.classbuddy.app.R;
import com.classbuddy.app.data.model.Routine;
import com.classbuddy.app.databinding.ItemAdminRoutineBinding;
import com.classbuddy.app.util.Constants;
import com.classbuddy.app.util.DateTimeUtils;

public class AdminRoutineAdapter extends ListAdapter<Routine, AdminRoutineAdapter.ViewHolder> {

    private final OnRoutineActionListener listener;

    public interface OnRoutineActionListener {
        void onEditClick(Routine routine);
        void onDeleteClick(Routine routine);
        void onCancelClick(Routine routine);
    }

    public AdminRoutineAdapter(OnRoutineActionListener listener) {
        super(DIFF_CALLBACK);
        this.listener = listener;
    }

    private static final DiffUtil.ItemCallback<Routine> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<Routine>() {
                @Override
                public boolean areItemsTheSame(@NonNull Routine oldItem, @NonNull Routine newItem) {
                    return oldItem.getId().equals(newItem.getId());
                }

                @Override
                public boolean areContentsTheSame(@NonNull Routine oldItem, @NonNull Routine newItem) {
                    return oldItem.getSubject().equals(newItem.getSubject()) &&
                            oldItem.getStartTime().equals(newItem.getStartTime());
                }
            };

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemAdminRoutineBinding binding = ItemAdminRoutineBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(getItem(position));
    }

    class ViewHolder extends RecyclerView. ViewHolder {
        private final ItemAdminRoutineBinding binding;

        ViewHolder(ItemAdminRoutineBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(Routine routine) {
            binding.tvSubject.setText(routine.getSubject());
            binding.tvFaculty.setText(routine.getFaculty());
            binding.tvRoom.setText("Room: " + routine.getRoom());
            binding.tvTime.setText(DateTimeUtils.formatTime(routine.getStartTime()) +
                    " - " + DateTimeUtils.formatTime(routine.getEndTime()));

            // Show day/date info based on recurring status
            if (routine.isRecurring() || routine.getSpecificDate() == null || routine.getSpecificDate().isEmpty()) {
                // Recurring weekly class - show day of week
                binding.tvDay.setText(routine.getDayOfWeek() + " (Weekly)");
            } else {
                // One-time class - show specific date
                binding.tvDay.setText(routine.getSpecificDate() + " (One-time)");
            }

            // Set type
            binding.chipType.setText(routine.getType().toUpperCase());
            int chipColor;
            switch (routine.getType().toLowerCase()) {
                case Constants.ROUTINE_TYPE_LAB:
                    chipColor = R.color.event_lab;
                    break;
                case Constants.ROUTINE_TYPE_TUTORIAL:
                    chipColor = R.color.secondary;
                    break;
                default:
                    chipColor = R.color.primary;
            }
            binding.chipType.setChipBackgroundColorResource(chipColor);

            // Handle cancelled status
            if (routine.isCancelled()) {
                binding.getRoot().setAlpha(0.6f);
                binding.tvSubject.setText(routine.getSubject() + " (CANCELLED)");
                binding.btnCancel.setText(R.string.restore_class);
            } else {
                binding.getRoot().setAlpha(1.0f);
                binding.btnCancel.setText(R.string.cancel_class);
            }

            binding.btnEdit.setOnClickListener(v -> {
                if (listener != null) listener.onEditClick(routine);
            });

            binding.btnDelete.setOnClickListener(v -> {
                if (listener != null) listener.onDeleteClick(routine);
            });

            binding.btnCancel.setOnClickListener(v -> {
                if (listener != null) listener.onCancelClick(routine);
            });
        }
    }
}
