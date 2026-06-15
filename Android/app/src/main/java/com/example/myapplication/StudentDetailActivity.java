package com.example.myapplication;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.myapplication.adapters.AttendanceRecordAdapter;
import com.example.myapplication.models.AttendanceRecord;
import com.example.myapplication.models.StudentDetailResponse;
import com.example.myapplication.network.RetrofitClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class StudentDetailActivity extends AppCompatActivity {

    private TextView btnBack, tvDetailStudentName, tvDetailStudentRoll, tvDetailPercentage, tvDetailRatio;
    private SwipeRefreshLayout detailSwipeRefresh;
    private RecyclerView rvStudentDetail;
    private AttendanceRecordAdapter adapter;

    private int studentId;
    private List<AttendanceRecord> recordsList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_detail);

        // Get details from Intent
        studentId = getIntent().getIntExtra("student_id", -1);
        String name = getIntent().getStringExtra("student_name");
        String roll = getIntent().getStringExtra("student_roll");

        if (studentId == -1) {
            Toast.makeText(this, "Invalid student details", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Bind Views
        btnBack = findViewById(R.id.btnBack);
        tvDetailStudentName = findViewById(R.id.tvDetailStudentName);
        tvDetailStudentRoll = findViewById(R.id.tvDetailStudentRoll);
        tvDetailPercentage = findViewById(R.id.tvDetailPercentage);
        tvDetailRatio = findViewById(R.id.tvDetailRatio);
        detailSwipeRefresh = findViewById(R.id.detailSwipeRefresh);
        rvStudentDetail = findViewById(R.id.rvStudentDetail);

        // Setup Init details
        tvDetailStudentName.setText(name);
        tvDetailStudentRoll.setText("Roll No: " + (roll != null ? roll : "N/A"));

        // Setup RecyclerView
        rvStudentDetail.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AttendanceRecordAdapter(recordsList);
        rvStudentDetail.setAdapter(adapter);

        // Click actions
        btnBack.setOnClickListener(v -> finish());
        
        detailSwipeRefresh.setOnRefreshListener(this::loadStudentDetail);

        loadStudentDetail();
    }

    private void loadStudentDetail() {
        detailSwipeRefresh.setRefreshing(true);
        
        RetrofitClient.getApiService(this).getStudentDetail(studentId).enqueue(new Callback<StudentDetailResponse>() {
            @Override
            public void onResponse(@NonNull Call<StudentDetailResponse> call, @NonNull Response<StudentDetailResponse> response) {
                detailSwipeRefresh.setRefreshing(false);
                if (response.isSuccessful() && response.body() != null) {
                    StudentDetailResponse detailRes = response.body();
                    if (detailRes.isSuccess()) {
                        recordsList = detailRes.getAttendance();
                        
                        // Calculate stats programmatically from list
                        int total = recordsList.size();
                        int attended = 0;
                        for (AttendanceRecord record : recordsList) {
                            if ("present".equalsIgnoreCase(record.getStatus())) {
                                attended++;
                            }
                        }
                        
                        double percent = total > 0 ? ((double) attended / total) * 100.0 : 100.0;
                        
                        // Set stats views
                        tvDetailPercentage.setText(String.format(Locale.US, "%.1f%%", percent));
                        if (percent < 75.0) {
                            tvDetailPercentage.setTextColor(getResources().getColor(R.color.absent_red));
                        } else {
                            tvDetailPercentage.setTextColor(getResources().getColor(R.color.present_green));
                        }
                        
                        tvDetailRatio.setText("Attended: " + attended + " / " + total);

                        // Bind List to Adapter
                        adapter = new AttendanceRecordAdapter(recordsList);
                        rvStudentDetail.setAdapter(adapter);
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<StudentDetailResponse> call, @NonNull Throwable t) {
                detailSwipeRefresh.setRefreshing(false);
                Toast.makeText(StudentDetailActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}
