package com.example.myapplication;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.myapplication.adapters.StudentAdapter;
import com.example.myapplication.models.ActiveSessionResponse;
import com.example.myapplication.models.ApiResponse;
import com.example.myapplication.models.CreateSessionRequest;
import com.example.myapplication.models.Session;
import com.example.myapplication.models.SummaryResponse;
import com.example.myapplication.network.RetrofitClient;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.Random;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TeacherDashboardActivity extends AppCompatActivity {

    private TextView tvTeacherName, tvActiveCode, btnLogout;
    private LinearLayout layoutActiveSession, layoutNoSession;
    private Button btnStartSession, btnCloseSession;
    private SwipeRefreshLayout swipeRefresh;
    private RecyclerView rvStudents;
    private StudentAdapter adapter;
    
    private SharedPreferences prefs;
    private Session activeSession = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teacher_dashboard);

        prefs = getSharedPreferences("app_settings", Context.MODE_PRIVATE);

        // Bind Views
        tvTeacherName = findViewById(R.id.tvTeacherName);
        tvActiveCode = findViewById(R.id.tvActiveCode);
        btnLogout = findViewById(R.id.btnLogout);
        layoutActiveSession = findViewById(R.id.layoutActiveSession);
        layoutNoSession = findViewById(R.id.layoutNoSession);
        btnStartSession = findViewById(R.id.btnStartSession);
        btnCloseSession = findViewById(R.id.btnCloseSession);
        swipeRefresh = findViewById(R.id.swipeRefresh);
        rvStudents = findViewById(R.id.rvStudents);

        // Setup Header
        String teacherName = prefs.getString("user_name", "Teacher");
        tvTeacherName.setText(teacherName);

        // Setup RecyclerView
        rvStudents.setLayoutManager(new LinearLayoutManager(this));
        adapter = new StudentAdapter(new ArrayList<>(), student -> {
            // Click student item -> Go to details view
            Intent intent = new Intent(TeacherDashboardActivity.this, StudentDetailActivity.class);
            intent.putExtra("student_id", student.getId());
            intent.putExtra("student_name", student.getName());
            intent.putExtra("student_roll", student.getRollNumber());
            startActivity(intent);
        });
        rvStudents.setAdapter(adapter);

        // Refresh action
        swipeRefresh.setOnRefreshListener(this::loadData);

        // Start session button action
        btnStartSession.setOnClickListener(v -> handleStartSession());

        // Close session button action
        btnCloseSession.setOnClickListener(v -> handleCloseSession());

        // Logout action
        btnLogout.setOnClickListener(v -> handleLogout());
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadData();
    }

    private void loadData() {
        swipeRefresh.setRefreshing(true);
        checkActiveSession();
        loadStudentSummary();
    }

    private void checkActiveSession() {
        RetrofitClient.getApiService(this).getActiveSession().enqueue(new Callback<ActiveSessionResponse>() {
            @Override
            public void onResponse(@NonNull Call<ActiveSessionResponse> call, @NonNull Response<ActiveSessionResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ActiveSessionResponse activeRes = response.body();
                    if (activeRes.isSuccess() && activeRes.getSession() != null) {
                        activeSession = activeRes.getSession();
                        showActiveSessionUI(activeSession.getSessionCode());
                    } else {
                        activeSession = null;
                        showNoSessionUI();
                    }
                } else {
                    showNoSessionUI();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ActiveSessionResponse> call, @NonNull Throwable t) {
                Toast.makeText(TeacherDashboardActivity.this, "Failed to get active session status.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadStudentSummary() {
        RetrofitClient.getApiService(this).getAttendanceSummary().enqueue(new Callback<SummaryResponse>() {
            @Override
            public void onResponse(@NonNull Call<SummaryResponse> call, @NonNull Response<SummaryResponse> response) {
                swipeRefresh.setRefreshing(false);
                if (response.isSuccessful() && response.body() != null) {
                    SummaryResponse summaryResponse = response.body();
                    if (summaryResponse.isSuccess() && summaryResponse.getStudents() != null) {
                        // Replace list and update adapter
                        adapter = new StudentAdapter(summaryResponse.getStudents(), student -> {
                            Intent intent = new Intent(TeacherDashboardActivity.this, StudentDetailActivity.class);
                            intent.putExtra("student_id", student.getId());
                            intent.putExtra("student_name", student.getName());
                            intent.putExtra("student_roll", student.getRollNumber());
                            startActivity(intent);
                        });
                        rvStudents.setAdapter(adapter);
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<SummaryResponse> call, @NonNull Throwable t) {
                swipeRefresh.setRefreshing(false);
                Toast.makeText(TeacherDashboardActivity.this, "Failed to load student attendance summary.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void handleStartSession() {
        // Generate random 4 digit code
        Random rand = new Random();
        int codeInt = 1000 + rand.nextInt(9000);
        String code = String.valueOf(codeInt);

        // Get current date formatted
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String dateStr = sdf.format(new Date());

        CreateSessionRequest request = new CreateSessionRequest(dateStr, code);
        btnStartSession.setEnabled(false);

        RetrofitClient.getApiService(this).createSession(request).enqueue(new Callback<ActiveSessionResponse>() {
            @Override
            public void onResponse(@NonNull Call<ActiveSessionResponse> call, @NonNull Response<ActiveSessionResponse> response) {
                btnStartSession.setEnabled(true);
                if (response.isSuccessful() && response.body() != null) {
                    ActiveSessionResponse activeRes = response.body();
                    if (activeRes.isSuccess() && activeRes.getSession() != null) {
                        activeSession = activeRes.getSession();
                        showActiveSessionUI(activeSession.getSessionCode());
                        Toast.makeText(TeacherDashboardActivity.this, "Session created successfully!", Toast.LENGTH_SHORT).show();
                        loadStudentSummary(); // Refresh summary as new session adds absent records
                    }
                } else {
                    Toast.makeText(TeacherDashboardActivity.this, "Failed to start session.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ActiveSessionResponse> call, @NonNull Throwable t) {
                btnStartSession.setEnabled(true);
                Toast.makeText(TeacherDashboardActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void handleCloseSession() {
        if (activeSession == null) return;

        btnCloseSession.setEnabled(false);
        RetrofitClient.getApiService(this).closeSession(activeSession.getId()).enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse> call, @NonNull Response<ApiResponse> response) {
                btnCloseSession.setEnabled(true);
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse apiRes = response.body();
                    if (apiRes.isSuccess()) {
                        activeSession = null;
                        showNoSessionUI();
                        Toast.makeText(TeacherDashboardActivity.this, "Session closed successfully.", Toast.LENGTH_SHORT).show();
                        loadData();
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse> call, @NonNull Throwable t) {
                btnCloseSession.setEnabled(true);
                Toast.makeText(TeacherDashboardActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void showActiveSessionUI(String code) {
        layoutNoSession.setVisibility(View.GONE);
        layoutActiveSession.setVisibility(View.VISIBLE);
        tvActiveCode.setText(code);
    }

    private void showNoSessionUI() {
        layoutActiveSession.setVisibility(View.GONE);
        layoutNoSession.setVisibility(View.VISIBLE);
    }

    private void handleLogout() {
        // Clear prefs
        prefs.edit().remove("user_id").remove("username").remove("user_name").remove("user_role").remove("user_roll_number").apply();
        
        Intent intent = new Intent(TeacherDashboardActivity.this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
}
