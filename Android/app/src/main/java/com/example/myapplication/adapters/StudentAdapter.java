package com.example.myapplication.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.models.AttendanceSummary;

import java.util.List;

public class StudentAdapter extends RecyclerView.Adapter<StudentAdapter.ViewHolder> {

    private final List<AttendanceSummary> studentList;
    private final OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(AttendanceSummary student);
    }

    public StudentAdapter(List<AttendanceSummary> studentList, OnItemClickListener listener) {
        this.studentList = studentList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_student, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        AttendanceSummary student = studentList.get(position);
        holder.bind(student, listener);
    }

    @Override
    public int getItemCount() {
        return studentList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvInitial;
        private final TextView tvName;
        private final TextView tvRollNumber;
        private final TextView tvPercentage;
        private final TextView tvRatio;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvInitial = itemView.findViewById(R.id.tvStudentInitial);
            tvName = itemView.findViewById(R.id.tvItemName);
            tvRollNumber = itemView.findViewById(R.id.tvItemRollNumber);
            tvPercentage = itemView.findViewById(R.id.tvItemPercentage);
            tvRatio = itemView.findViewById(R.id.tvItemRatio);
        }

        public void bind(final AttendanceSummary student, final OnItemClickListener listener) {
            tvName.setText(student.getName());
            tvRollNumber.setText("Roll No: " + (student.getRollNumber() != null ? student.getRollNumber() : "N/A"));
            
            // Set initials
            if (student.getName() != null && !student.getName().isEmpty()) {
                tvInitial.setText(String.valueOf(student.getName().charAt(0)).toUpperCase());
            } else {
                tvInitial.setText("?");
            }

            // Attendance percentage
            double percent = student.getAttendancePercentage();
            tvPercentage.setText(String.format("%.1f%%", percent));

            // Color code percentage: Red if below 75%, green otherwise
            if (percent < 75.0) {
                tvPercentage.setTextColor(itemView.getContext().getResources().getColor(R.color.absent_red));
            } else {
                tvPercentage.setTextColor(itemView.getContext().getResources().getColor(R.color.present_green));
            }

            tvRatio.setText(student.getAttendedSessions() + "/" + student.getTotalSessions() + " classes");

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onItemClick(student);
                }
            });
        }
    }
}
