package com.classbuddy.app.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.classbuddy.app.R;
import com.classbuddy.app.data.model.Exam;
import com.classbuddy.app.databinding.ItemExamBinding;
import com.classbuddy.app.util.DateTimeUtils;

public class ExamAdapter extends ListAdapter<Exam, ExamAdapter. ExamViewHolder> {

    private final OnExamClickListener listener;

    public interface OnExamClickListener {
        void onExamClick(Exam exam);
    }

    public ExamAdapter(OnExamClickListener listener) {
        super(DIFF_CALLBACK);
        this.listener = listener;
    }

    private static final DiffUtil.ItemCallback<Exam> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<Exam>() {
                @Override
                public boolean areItemsTheSame(@NonNull Exam oldItem, @NonNull Exam newItem) {
                    return oldItem. getId().equals(newItem.getId());
                }

                @Override
                public boolean areContentsTheSame(@NonNull Exam oldItem, @NonNull Exam newItem) {
                    return oldItem.getCourseName().equals(newItem.getCourseName());
                }
            };

    @NonNull
    @Override
    public ExamViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemExamBinding binding = ItemExamBinding.inflate(
                LayoutInflater.from(parent. getContext()), parent, false);
        return new ExamViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ExamViewHolder holder, int position) {
        holder.bind(getItem(position));
    }

    class ExamViewHolder extends RecyclerView.ViewHolder {
        private final ItemExamBinding binding;

        ExamViewHolder(ItemExamBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(Exam exam) {
            binding.tvCourseName.setText(exam.getCourseName());
            binding.tvCourseNo.setText(exam.getCourseNo());
            binding.tvExamType.setText(exam.getExamTypeDisplay());
            binding.tvDate.setText(DateTimeUtils.formatDate(exam.getExamDate()));
            binding.tvTime.setText(DateTimeUtils.formatTime(exam.getStartTime()));

            // Handle cancelled status
            if (exam.isCancelled()) {
                binding.tvCountdown.setText("CANCELLED");
                binding.tvCountdown.setBackgroundTintList(
                        android.content.res.ColorStateList.valueOf(
                                binding.getRoot().getContext().getColor(R.color.error)));
                binding.getRoot().setAlpha(0.7f);
            } else {
                binding.tvCountdown.setText(DateTimeUtils.getCountdown(exam.getExamDate()));
                binding.tvCountdown.setBackgroundTintList(null);
                binding.getRoot().setAlpha(1.0f);
            }

            // Room
            String room = exam.getRoom();
            if (room != null && !room.isEmpty()) {
                binding.tvRoom.setText("Room " + room);
            } else {
                binding.tvRoom.setText("TBA");
            }

            // Set exam type badge color
            int badgeColor;
            switch (exam.getExamType().toLowerCase()) {
                case "ct":
                    badgeColor = R.color.exam_ct;
                    break;
                case "final":
                    badgeColor = R.color.exam_final;
                    break;
                case "labquiz":
                    badgeColor = R.color.exam_labquiz;
                    break;
                case "viva":
                    badgeColor = R.color.exam_viva;
                    break;
                default:
                    badgeColor = R.color.primary;
            }
            binding. tvExamType.setBackgroundTintList(
                    android.content.res.ColorStateList.valueOf(
                            binding.getRoot().getContext().getColor(badgeColor)));

            binding.getRoot().setOnClickListener(v -> {
                if (listener != null) {
                    listener.onExamClick(exam);
                }
            });
        }
    }
}
