package com.example.myapplication;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.myapplication.adapters.AttendanceRecordAdapter;
import com.example.myapplication.models.ActiveSessionResponse;
import com.example.myapplication.models.ApiResponse;
import com.example.myapplication.models.AttendanceRecord;
import com.example.myapplication.models.Session;
import com.example.myapplication.models.StudentAttendanceResponse;
import com.example.myapplication.models.SubmitAttendanceRequest;
import com.example.myapplication.network.RetrofitClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class StudentDashboardActivity extends AppCompatActivity {

    private TextView tvStudentName, tvStudentRoll, tvStudentPercentage, tvStudentRatio, btnStudentLogout;
    private TextView tvSubmitTitle, tvSubmitFeedback;
    private LinearLayout layoutSubmitForm;
    private EditText etSessionCode;
    private Button btnSubmitCode;
    private SwipeRefreshLayout studentSwipeRefresh;
    private RecyclerView rvStudentHistory;
    private AttendanceRecordAdapter adapter;

    private SharedPreferences prefs;
    private int studentId;
    private Session activeSession = null;
    private List<AttendanceRecord> historyList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_dashboard);

        prefs = getSharedPreferences("app_settings", Context.MODE_PRIVATE);
        studentId = prefs.getInt("user_id", -1);

        if (studentId == -1) {
            // Failsafe
            handleLogout();
            return;
        }

        // Bind Views
        tvStudentName = findViewById(R.id.tvStudentName);
        tvStudentRoll = findViewById(R.id.tvStudentRoll);
        tvStudentPercentage = findViewById(R.id.tvStudentPercentage);
        tvStudentRatio = findViewById(R.id.tvStudentRatio);
        
        tvSubmitTitle = findViewById(R.id.tvSubmitTitle);
        tvSubmitFeedback = findViewById(R.id.tvSubmitFeedback);
        layoutSubmitForm = findViewById(R.id.layoutSubmitForm);
        etSessionCode = findViewById(R.id.etSessionCode);
        btnSubmitCode = findViewById(R.id.btnSubmitCode);
        btnStudentLogout = findViewById(R.id.btnStudentLogout);
        
        studentSwipeRefresh = findViewById(R.id.studentSwipeRefresh);
        rvStudentHistory = findViewById(R.id.rvStudentHistory);

        // Setup Header Details
        tvStudentName.setText(prefs.getString("user_name", "Student"));
        tvStudentRoll.setText("Roll No: " + prefs.getString("user_roll_number", "N/A"));

        // Setup RecyclerView
        rvStudentHistory.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AttendanceRecordAdapter(historyList);
        rvStudentHistory.setAdapter(adapter);

        // Swipe Refresh
        studentSwipeRefresh.setOnRefreshListener(this::loadDashboardData);

        // Submit Code Action
        btnSubmitCode.setOnClickListener(v -> handleSubmitCode());

        // Logout Action
        btnStudentLogout.setOnClickListener(v -> handleLogout());
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadDashboardData();
    }

    private void loadDashboardData() {
        studentSwipeRefresh.setRefreshing(true);
        loadStudentAttendanceStats();
    }

    private void loadStudentAttendanceStats() {
        RetrofitClient.getApiService(this).getStudentAttendance(studentId).enqueue(new Callback<StudentAttendanceResponse>() {
            @Override
            public void onResponse(@NonNull Call<StudentAttendanceResponse> call, @NonNull Response<StudentAttendanceResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    StudentAttendanceResponse attendanceRes = response.body();
                    if (attendanceRes.isSuccess()) {
                        // Bind Summary Details
                        StudentAttendanceResponse.AttendanceSummaryInfo summary = attendanceRes.getSummary();
                        tvStudentPercentage.setText(String.format(Locale.US, "%.1f%%", summary.getAttendancePercentage()));
                        tvStudentRatio.setText("Attended: " + summary.getAttendedSessions() + " / " + summary.getTotalSessions() + " sessions");

                        // Bind History List
                        historyList = attendanceRes.getHistory();
                        adapter = new AttendanceRecordAdapter(historyList);
                        rvStudentHistory.setAdapter(adapter);
                    }
                }
                // Check active session next
                checkActiveSession();
            }

            @Override
            public void onFailure(@NonNull Call<StudentAttendanceResponse> call, @NonNull Throwable t) {
                Toast.makeText(StudentDashboardActivity.this, "Failed to load stats: " + t.getMessage(), Toast.LENGTH_LONG).show();
                checkActiveSession();
            }
        });
    }

    private void checkActiveSession() {
        RetrofitClient.getApiService(this).getActiveSession().enqueue(new Callback<ActiveSessionResponse>() {
            @Override
            public void onResponse(@NonNull Call<ActiveSessionResponse> call, @NonNull Response<ActiveSessionResponse> response) {
                studentSwipeRefresh.setRefreshing(false);
                if (response.isSuccessful() && response.body() != null) {
                    ActiveSessionResponse activeRes = response.body();
                    if (activeRes.isSuccess() && activeRes.getSession() != null) {
                        activeSession = activeRes.getSession();
                        
                        // Check if student has already submitted attendance for this active session
                        boolean alreadySubmitted = false;
                        for (AttendanceRecord record : historyList) {
                            if (record.getSessionId() == activeSession.getId() && "present".equalsIgnoreCase(record.getStatus())) {
                                alreadySubmitted = true;
                                break;
                            }
                        }

                        if (alreadySubmitted) {
                            layoutSubmitForm.setVisibility(View.GONE);
                            tvSubmitFeedback.setVisibility(View.VISIBLE);
                            tvSubmitFeedback.setText("Attendance Marked: PRESENT ✅");
                            tvSubmitFeedback.setTextColor(getResources().getColor(R.color.present_green));
                        } else {
                            tvSubmitFeedback.setVisibility(View.GONE);
                            layoutSubmitForm.setVisibility(View.VISIBLE);
                        }
                    } else {
                        // No active session
                        activeSession = null;
                        layoutSubmitForm.setVisibility(View.GONE);
                        tvSubmitFeedback.setVisibility(View.VISIBLE);
                        tvSubmitFeedback.setText("No active class attendance session right now.");
                        tvSubmitFeedback.setTextColor(getResources().getColor(R.color.gray_text));
                    }
                } else {
                    layoutSubmitForm.setVisibility(View.GONE);
                    tvSubmitFeedback.setVisibility(View.VISIBLE);
                    tvSubmitFeedback.setText("Unable to fetch active session details.");
                    tvSubmitFeedback.setTextColor(getResources().getColor(R.color.gray_text));
                }
            }

            @Override
            public void onFailure(@NonNull Call<ActiveSessionResponse> call, @NonNull Throwable t) {
                studentSwipeRefresh.setRefreshing(false);
                layoutSubmitForm.setVisibility(View.GONE);
                tvSubmitFeedback.setVisibility(View.VISIBLE);
                tvSubmitFeedback.setText("Network error fetching session status.");
                tvSubmitFeedback.setTextColor(getResources().getColor(R.color.gray_text));
            }
        });
    }

    private void handleSubmitCode() {
        String code = etSessionCode.getText().toString().trim();
        if (TextUtils.isEmpty(code)) {
            etSessionCode.setError("Please enter the 4-digit code");
            return;
        }

        btnSubmitCode.setEnabled(false);
        SubmitAttendanceRequest request = new SubmitAttendanceRequest(studentId, code);

        RetrofitClient.getApiService(this).submitAttendance(request).enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse> call, @NonNull Response<ApiResponse> response) {
                btnSubmitCode.setEnabled(true);
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse apiRes = response.body();
                    if (apiRes.isSuccess()) {
                        etSessionCode.setText("");
                        Toast.makeText(StudentDashboardActivity.this, apiRes.getMessage(), Toast.LENGTH_LONG).show();
                        loadDashboardData(); // Refresh history list and stats
                    } else {
                        Toast.makeText(StudentDashboardActivity.this, apiRes.getMessage(), Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(StudentDashboardActivity.this, "Submission failed. Please check the code.", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse> call, @NonNull Throwable t) {
                btnSubmitCode.setEnabled(true);
                Toast.makeText(StudentDashboardActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void handleLogout() {
        // Clear prefs
        prefs.edit().remove("user_id").remove("username").remove("user_name").remove("user_role").remove("user_roll_number").apply();
        
        Intent intent = new Intent(StudentDashboardActivity.this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
}
