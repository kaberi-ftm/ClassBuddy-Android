package com.classbuddy.app.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.classbuddy.app.R;
import com.classbuddy.app.data.model.Notice;
import com.classbuddy.app.databinding.ItemNoticeBinding;
import com.classbuddy.app.util.DateTimeUtils;

public class NoticeAdapter extends ListAdapter<Notice, NoticeAdapter.NoticeViewHolder> {

    private final OnNoticeClickListener listener;
    private String currentUserId;

    public interface OnNoticeClickListener {
        void onNoticeClick(Notice notice, int position);
    }

    public NoticeAdapter(OnNoticeClickListener listener) {
        super(DIFF_CALLBACK);
        this.listener = listener;
    }

    public void setCurrentUserId(String userId) {
        this.currentUserId = userId;
    }

    private static final DiffUtil.ItemCallback<Notice> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<Notice>() {
                @Override
                public boolean areItemsTheSame(@NonNull Notice oldItem, @NonNull Notice newItem) {
                    return oldItem.getId().equals(newItem.getId());
                }

                @Override
                public boolean areContentsTheSame(@NonNull Notice oldItem, @NonNull Notice newItem) {
                    return oldItem.getTitle().equals(newItem.getTitle()) &&
                            oldItem.isPinned() == newItem.isPinned();
                }
            };

    @NonNull
    @Override
    public NoticeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemNoticeBinding binding = ItemNoticeBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new NoticeViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull NoticeViewHolder holder, int position) {
        holder.bind(getItem(position));
    }

    class NoticeViewHolder extends RecyclerView.ViewHolder {
        private final ItemNoticeBinding binding;

        NoticeViewHolder(ItemNoticeBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(Notice notice) {
            // Set title
            binding.tvTitle.setText(notice.getTitle());

            // Set content
            binding.tvContent.setText(notice.getContent());

            // Set classroom name
            binding.tvClassroom. setText(notice.getClassroomName());

            // Set time
            binding.tvTime.setText(DateTimeUtils.getRelativeTime(notice.getCreatedAt()));

            // Set author
            binding.tvAuthor. setText("By " + notice.getAdminName());

            // Pin indicator
            binding.ivPinned.setVisibility(notice.isPinned() ? View.VISIBLE : View. GONE);

            // Priority indicator color
            int priorityColor;
            String priority = notice.getPriority();
            if (priority == null) priority = "normal";

            switch (priority. toLowerCase()) {
                case "urgent":
                    priorityColor = R.color.priority_urgent;
                    break;
                case "high":
                    priorityColor = R.color.priority_high;
                    break;
                case "low":
                    priorityColor = R.color.priority_low;
                    break;
                default:
                    priorityColor = R.color.priority_normal;
            }
            binding.viewPriority.setBackgroundResource(priorityColor);

            // Image attachment
            String imageUrl = notice.getImageUrl();
            if (imageUrl != null && !imageUrl.isEmpty()) {
                binding.ivAttachment.setVisibility(View.VISIBLE);
                Glide.with(binding. getRoot().getContext())
                        .load(imageUrl)
                        . centerCrop()
                        . into(binding.ivAttachment);
            } else {
                binding. ivAttachment.setVisibility(View.GONE);
            }

            // Read status - make slightly transparent if read
            if (currentUserId != null && notice.isReadByStudent(currentUserId)) {
                binding.getRoot().setAlpha(0.7f);
            } else {
                binding.getRoot().setAlpha(1.0f);
            }

            // Click listener
            binding. getRoot().setOnClickListener(v -> {
                if (listener != null) {
                    listener.onNoticeClick(notice, getAdapterPosition());
                }
            });
        }
    }
}
