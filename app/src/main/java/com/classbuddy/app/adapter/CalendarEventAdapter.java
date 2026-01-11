package com.classbuddy.app.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.classbuddy.app.R;
import com.classbuddy.app.data.model.CalendarEvent;
import com.classbuddy.app.databinding.ItemCalendarEventBinding;

public class CalendarEventAdapter extends ListAdapter<CalendarEvent, CalendarEventAdapter.EventViewHolder> {

    private final OnEventClickListener listener;

    public interface OnEventClickListener {
        void onEventClick(CalendarEvent event);
    }

    public CalendarEventAdapter(OnEventClickListener listener) {
        super(DIFF_CALLBACK);
        this.listener = listener;
    }

    private static final DiffUtil.ItemCallback<CalendarEvent> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<CalendarEvent>() {
                @Override
                public boolean areItemsTheSame(@NonNull CalendarEvent oldItem, @NonNull CalendarEvent newItem) {
                    return oldItem. getId().equals(newItem.getId());
                }

                @Override
                public boolean areContentsTheSame(@NonNull CalendarEvent oldItem, @NonNull CalendarEvent newItem) {
                    return oldItem.getTitle().equals(newItem.getTitle()) &&
                            oldItem. getTime().equals(newItem.getTime());
                }
            };

    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemCalendarEventBinding binding = ItemCalendarEventBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new EventViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        holder.bind(getItem(position));
    }

    class EventViewHolder extends RecyclerView.ViewHolder {
        private final ItemCalendarEventBinding binding;

        EventViewHolder(ItemCalendarEventBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(CalendarEvent event) {
            binding.tvTitle.setText(event.getTitle());
            binding.tvTime.setText(event.getTime());
            binding.tvClassroom.setText(event.getClassroomName());
            binding. tvDescription.setText(event.getDescription());
            binding.tvEventType.setText(event.getEventType());

            // Set color based on event type
            int color;
            switch (event.getEventType().toLowerCase()) {
                case "exam":
                    color = R.color.event_exam;
                    break;
                case "lab":
                    color = R.color.event_lab;
                    break;
                case "class":
                default:
                    color = R. color.event_class;
            }
            binding.viewTypeIndicator.setBackgroundResource(color);
            binding.tvEventType.setTextColor(binding.getRoot().getContext().getColor(color));

            binding.getRoot().setOnClickListener(v -> {
                if (listener != null) {
                    listener. onEventClick(event);
                }
            });
        }
    }
}
