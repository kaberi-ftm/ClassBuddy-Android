package com.classbuddy.app.adapter;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.classbuddy.app.R;
import com.classbuddy.app.data.model.Classroom;
import com.classbuddy.app.databinding.ItemAdminClassroomBinding;
import com.classbuddy.app.util.CodeGenerator;

public class AdminClassroomAdapter extends ListAdapter<Classroom, AdminClassroomAdapter.ViewHolder> {

    private final OnClassroomActionListener listener;

    public interface OnClassroomActionListener {
        void onClassroomClick(Classroom classroom);
        void onViewStudentsClick(Classroom classroom);
        void onDeleteClick(Classroom classroom);
        void onShareClick(Classroom classroom);
    }

    public AdminClassroomAdapter(OnClassroomActionListener listener) {
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
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemAdminClassroomBinding binding = ItemAdminClassroomBinding.inflate(
                LayoutInflater. from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(getItem(position));
    }

    class ViewHolder extends RecyclerView. ViewHolder {
        private final ItemAdminClassroomBinding binding;

        ViewHolder(ItemAdminClassroomBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(Classroom classroom) {
            binding.tvClassName.setText(classroom.getName());
            binding.tvSection.setText(classroom.getSection() + " â€¢ " + classroom.getDepartment());
            binding.tvStudentCount.setText(classroom. getStudentCount() + " students");

            // Set initial letter
            String initial = classroom.getName().substring(0, 1).toUpperCase();
            binding. tvInitial.setText(initial);

            // Format and display code
            String formattedCode = CodeGenerator.formatCodeForDisplay(classroom.getCode());
            binding.tvCode.setText("Code: " + formattedCode);

            // Copy button
            binding.btnCopy.setOnClickListener(v -> {
                Context context = binding.getRoot().getContext();
                ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context. CLIPBOARD_SERVICE);
                String copyText = "Code: " + classroom.getCode() + "\nPassword: " + classroom.getPassword();
                ClipData clip = ClipData. newPlainText("Classroom Code", copyText);
                clipboard.setPrimaryClip(clip);
                Toast.makeText(context, R.string.copied, Toast.LENGTH_SHORT).show();
            });

            // Share button
            binding.btnShare.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onShareClick(classroom);
                }
            });

            // View students button
            binding.btnViewStudents.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onViewStudentsClick(classroom);
                }
            });

            // Delete button
            binding.btnDelete.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDeleteClick(classroom);
                }
            });

            // Root click
            binding.getRoot().setOnClickListener(v -> {
                if (listener != null) {
                    listener.onClassroomClick(classroom);
                }
            });
        }
    }
}
