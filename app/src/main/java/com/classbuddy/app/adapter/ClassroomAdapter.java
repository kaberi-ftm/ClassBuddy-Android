package com.classbuddy.app.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.classbuddy.app.data.model.Classroom;
import com.classbuddy.app.databinding.ItemClassroomBinding;

public class ClassroomAdapter extends ListAdapter<Classroom, ClassroomAdapter.ClassroomViewHolder> {

    private final OnClassroomClickListener listener;

    public interface OnClassroomClickListener {
        void onClassroomClick(Classroom classroom);
    }

    public ClassroomAdapter(OnClassroomClickListener listener) {
        super(DIFF_CALLBACK);
        this.listener = listener;
    }

    private static final DiffUtil.ItemCallback<Classroom> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<Classroom>() {
                @Override
                public boolean areItemsTheSame(@NonNull Classroom oldItem, @NonNull Classroom newItem) {
                    return oldItem.getId().equals(newItem.getId());
                }

                @Override
                public boolean areContentsTheSame(@NonNull Classroom oldItem, @NonNull Classroom newItem) {
                    return oldItem.getName().equals(newItem.getName()) &&
                            oldItem.getStudentCount() == newItem.getStudentCount();
                }
            };

    @NonNull
    @Override
    public ClassroomViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemClassroomBinding binding = ItemClassroomBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ClassroomViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ClassroomViewHolder holder, int position) {
        holder.bind(getItem(position));
    }

    class ClassroomViewHolder extends RecyclerView.ViewHolder {
        private final ItemClassroomBinding binding;

        ClassroomViewHolder(ItemClassroomBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(Classroom classroom) {
            binding.tvClassName.setText(classroom.getName());
            binding.tvSection.setText(classroom.getSection() + " â€¢ " + classroom.getDepartment());
            binding.tvStudentCount.setText(classroom. getStudentCount() + " students");
            binding.tvAdminName.setText("By " + classroom.getAdminName());

            // Set initial letter
            String initial = classroom.getName().substring(0, 1).toUpperCase();
            binding.tvInitial. setText(initial);

            binding.getRoot().setOnClickListener(v -> {
                if (listener != null) {
                    listener. onClassroomClick(classroom);
                }
            });
        }
    }
}
