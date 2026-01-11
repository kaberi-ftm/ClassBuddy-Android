package com.classbuddy.app.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.classbuddy.app.R;
import com.classbuddy.app.data.model.User;
import com.classbuddy.app.databinding.ItemStudentBinding;

public class StudentAdapter extends ListAdapter<User, StudentAdapter.StudentViewHolder> {

    private final OnStudentActionListener listener;

    public interface OnStudentActionListener {
        void onStudentClick(User student);
        void onRemoveClick(User student, int position);
        void onMessageClick(User student);
    }

    public StudentAdapter(OnStudentActionListener listener) {
        super(DIFF_CALLBACK);
        this.listener = listener;
    }

    private static final DiffUtil.ItemCallback<User> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<User>() {
                @Override
                public boolean areItemsTheSame(@NonNull User oldItem, @NonNull User newItem) {
                    return oldItem.getId().equals(newItem.getId());
                }

                @Override
                public boolean areContentsTheSame(@NonNull User oldItem, @NonNull User newItem) {
                    return oldItem.getFullName().equals(newItem.getFullName());
                }
            };

    @NonNull
    @Override
    public StudentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemStudentBinding binding = ItemStudentBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new StudentViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull StudentViewHolder holder, int position) {
        holder.bind(getItem(position));
    }

    class StudentViewHolder extends RecyclerView.ViewHolder {
        private final ItemStudentBinding binding;

        StudentViewHolder(ItemStudentBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(User student) {
            binding.tvName.setText(student.getFullName());
            binding.tvEmail. setText(student.getEmail());

            // Load profile image
            if (student.getProfileImageUrl() != null && !student.getProfileImageUrl().isEmpty()) {
                Glide.with(binding.getRoot().getContext())
                        .load(student.getProfileImageUrl())
                        .placeholder(R.drawable.ic_profile)
                        .circleCrop()
                        . into(binding.ivAvatar);
            } else {
                binding.ivAvatar.setImageResource(R.drawable.ic_profile);
            }

            binding.getRoot().setOnClickListener(v -> {
                if (listener != null) {
                    listener.onStudentClick(student);
                }
            });

            binding.btnRemove.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onRemoveClick(student, getAdapterPosition());
                }
            });

            binding.btnMessage.setOnClickListener(v -> {
                if (listener != null) {
                    listener. onMessageClick(student);
                }
            });
        }
    }
}
