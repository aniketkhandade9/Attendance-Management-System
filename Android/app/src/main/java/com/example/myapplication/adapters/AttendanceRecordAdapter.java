package com.example.myapplication.adapters;

import android.content.Context;
import android.graphics.PorterDuff;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.models.AttendanceRecord;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class AttendanceRecordAdapter extends RecyclerView.Adapter<AttendanceRecordAdapter.ViewHolder> {

    private final List<AttendanceRecord> recordsList;

    public AttendanceRecordAdapter(List<AttendanceRecord> recordsList) {
        this.recordsList = recordsList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_attendance_record, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        AttendanceRecord record = recordsList.get(position);
        holder.bind(record);
    }

    @Override
    public int getItemCount() {
        return recordsList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvDate;
        private final TextView tvCode;
        private final TextView tvTime;
        private final TextView tvStatus;
        private final FrameLayout statusBadge;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDate = itemView.findViewById(R.id.tvRecordDate);
            tvCode = itemView.findViewById(R.id.tvRecordCode);
            tvTime = itemView.findViewById(R.id.tvRecordTime);
            tvStatus = itemView.findViewById(R.id.tvRecordStatus);
            statusBadge = itemView.findViewById(R.id.statusBadge);
        }

        public void bind(AttendanceRecord record) {
            Context context = itemView.getContext();
            
            // Format date
            tvDate.setText(formatDate(record.getDate()));
            
            // Code
            tvCode.setText("Class Code: " + record.getSessionCode());

            // Status Badge Formatting
            String status = record.getStatus();
            if ("present".equalsIgnoreCase(status)) {
                tvStatus.setText("Present");
                tvStatus.setTextColor(ContextCompat.getColor(context, R.color.present_green));
                statusBadge.getBackground().setColorFilter(
                        ContextCompat.getColor(context, R.color.present_green_light), 
                        PorterDuff.Mode.SRC_IN
                );
                
                // Show submission time
                tvTime.setVisibility(View.VISIBLE);
                tvTime.setText("Submitted: " + formatTime(record.getSubmittedAt()));
            } else {
                tvStatus.setText("Absent");
                tvStatus.setTextColor(ContextCompat.getColor(context, R.color.absent_red));
                statusBadge.getBackground().setColorFilter(
                        ContextCompat.getColor(context, R.color.absent_red_light), 
                        PorterDuff.Mode.SRC_IN
                );
                
                // Hide time or show absent
                tvTime.setVisibility(View.VISIBLE);
                tvTime.setText("No submission recorded");
            }
        }

        private String formatDate(String dateStr) {
            if (dateStr == null || dateStr.isEmpty()) return "N/A";
            try {
                SimpleDateFormat inputFormat;
                if (dateStr.contains("T")) {
                    inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);
                    inputFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
                } else {
                    inputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
                }
                Date date = inputFormat.parse(dateStr);
                SimpleDateFormat outputFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.US);
                return outputFormat.format(date);
            } catch (Exception e) {
                return dateStr;
            }
        }

        private String formatTime(String timeStr) {
            if (timeStr == null || timeStr.isEmpty()) return "N/A";
            try {
                SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);
                inputFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
                Date date = inputFormat.parse(timeStr);
                
                // Format to local device timezone
                SimpleDateFormat outputFormat = new SimpleDateFormat("hh:mm a", Locale.US);
                outputFormat.setTimeZone(TimeZone.getDefault());
                return outputFormat.format(date);
            } catch (Exception e) {
                // Secondary check for simple time strings if server time formats differ
                try {
                    SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
                    Date date = inputFormat.parse(timeStr);
                    SimpleDateFormat outputFormat = new SimpleDateFormat("hh:mm a", Locale.US);
                    return outputFormat.format(date);
                } catch (Exception ex) {
                    return timeStr;
                }
            }
        }
    }
}
