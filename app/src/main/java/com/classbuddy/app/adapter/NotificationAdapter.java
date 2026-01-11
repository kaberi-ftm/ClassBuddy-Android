package com.classbuddy.app.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.classbuddy.app.R;
import com.classbuddy.app.data.model.Notification;
import com.classbuddy.app.databinding.ItemNotificationBinding;
import com.classbuddy.app.util.Constants;
import com.classbuddy.app.util.DateTimeUtils;

public class NotificationAdapter extends ListAdapter<Notification, NotificationAdapter. NotificationViewHolder> {

    private final OnNotificationClickListener listener;

    public interface OnNotificationClickListener {
        void onNotificationClick(Notification notification, int position);
    }

    public NotificationAdapter(OnNotificationClickListener listener) {
        super(DIFF_CALLBACK);
        this.listener = listener;
    }

    private static final DiffUtil.ItemCallback<Notification> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<Notification>() {
                @Override
                public boolean areItemsTheSame(@NonNull Notification oldItem, @NonNull Notification newItem) {
                    return oldItem.getId().equals(newItem.getId());
                }

                @Override
                public boolean areContentsTheSame(@NonNull Notification oldItem, @NonNull Notification newItem) {
                    return oldItem.isRead() == newItem.isRead() &&
                            oldItem. getTitle().equals(newItem.getTitle());
                }
            };

    @NonNull
    @Override
    public NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemNotificationBinding binding = ItemNotificationBinding.inflate(
                LayoutInflater.from(parent. getContext()), parent, false);
        return new NotificationViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationViewHolder holder, int position) {
        holder.bind(getItem(position));
    }

    class NotificationViewHolder extends RecyclerView.ViewHolder {
        private final ItemNotificationBinding binding;

        NotificationViewHolder(ItemNotificationBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(Notification notification) {
            binding.tvTitle.setText(notification.getTitle());
            binding.tvMessage.setText(notification.getMessage());
            binding.tvTime.setText(DateTimeUtils.getRelativeTime(notification.getCreatedAt()));

            // Show/hide unread indicator
            binding.viewUnread.setVisibility(notification.isRead() ? View.GONE : View.VISIBLE);

            // Set icon based on type
            int iconRes;
            int iconTint;
            String type = notification.getType();
            if (type == null) type = "";

            switch (type) {
                case Constants.NOTIFICATION_TYPE_EXAM:
                    iconRes = R.drawable.ic_exam;
                    iconTint = R. color.exam_ct;
                    break;
                case Constants. NOTIFICATION_TYPE_NOTICE:
                    iconRes = R.drawable.ic_notice;
                    iconTint = R. color.secondary;
                    break;
                case Constants.NOTIFICATION_TYPE_CLASSROOM:
                    iconRes = R.drawable.ic_classroom;
                    iconTint = R. color.primary;
                    break;
                case Constants.NOTIFICATION_TYPE_REMINDER:
                    iconRes = R.drawable.ic_notification;
                    iconTint = R.color.warning;
                    break;
                default:
                    iconRes = R.drawable.ic_notification;
                    iconTint = R.color.primary;
            }
            binding.ivIcon. setImageResource(iconRes);
            binding.ivIcon.setColorFilter(binding.getRoot().getContext().getColor(iconTint));

            // Set alpha for read notifications
            binding.getRoot().setAlpha(notification. isRead() ? 0.7f : 1.0f);

            binding.getRoot().setOnClickListener(v -> {
                if (listener != null) {
                    listener.onNotificationClick(notification, getAdapterPosition());
                }
            });
        }
    }
}
