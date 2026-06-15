package com.example.myapplication;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.models.LoginRequest;
import com.example.myapplication.models.LoginResponse;
import com.example.myapplication.models.User;
import com.example.myapplication.network.RetrofitClient;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    private EditText etUsername, etPassword;
    private Button btnLogin;
    private TextView btnGoToRegister, btnSettings;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        prefs = getSharedPreferences("app_settings", Context.MODE_PRIVATE);

        // Check if user is already logged in
        checkAutoLogin();

        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnGoToRegister = findViewById(R.id.btnGoToRegister);
        btnSettings = findViewById(R.id.btnSettings);

        btnLogin.setOnClickListener(v -> handleLogin());
        btnGoToRegister.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });

        btnSettings.setOnClickListener(v -> showIpSettingsDialog());
    }

    private void checkAutoLogin() {
        int loggedUserId = prefs.getInt("user_id", -1);
        String role = prefs.getString("user_role", "");

        if (loggedUserId != -1 && !TextUtils.isEmpty(role)) {
            navigateToDashboard(role);
            finish();
        }
    }

    private void handleLogin() {
        String username = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (TextUtils.isEmpty(username)) {
            etUsername.setError("Username is required");
            return;
        }
        if (TextUtils.isEmpty(password)) {
            etPassword.setError("Password is required");
            return;
        }

        btnLogin.setEnabled(false);
        btnLogin.setText("LOGGING IN...");

        LoginRequest request = new LoginRequest(username, password);
        RetrofitClient.getApiService(this).login(request).enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(@NonNull Call<LoginResponse> call, @NonNull Response<LoginResponse> response) {
                btnLogin.setEnabled(true);
                btnLogin.setText("LOG IN");

                if (response.isSuccessful() && response.body() != null) {
                    LoginResponse loginResponse = response.body();
                    if (loginResponse.isSuccess() && loginResponse.getUser() != null) {
                        User user = loginResponse.getUser();
                        
                        // Save to prefs for auto login
                        SharedPreferences.Editor editor = prefs.edit();
                        editor.putInt("user_id", user.getId());
                        editor.putString("username", user.getUsername());
                        editor.putString("user_name", user.getName());
                        editor.putString("user_role", user.getRole());
                        editor.putString("user_roll_number", user.getRollNumber());
                        editor.apply();

                        Toast.makeText(LoginActivity.this, "Welcome " + user.getName(), Toast.LENGTH_SHORT).show();
                        navigateToDashboard(user.getRole());
                        finish();
                    } else {
                        Toast.makeText(LoginActivity.this, loginResponse.getMessage(), Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(LoginActivity.this, "Login failed. Check server or credentials.", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<LoginResponse> call, @NonNull Throwable t) {
                btnLogin.setEnabled(true);
                btnLogin.setText("LOG IN");
                Toast.makeText(LoginActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void navigateToDashboard(String role) {
        Intent intent;
        if ("teacher".equalsIgnoreCase(role)) {
            intent = new Intent(LoginActivity.this, TeacherDashboardActivity.class);
        } else {
            intent = new Intent(LoginActivity.this, StudentDashboardActivity.class);
        }
        startActivity(intent);
    }

    private void showIpSettingsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Server URL Settings");
        builder.setMessage("Configure backend REST API Server IP & Port:");

        // Simple text input in dialog
        final EditText input = new EditText(this);
        input.setPadding(40, 20, 40, 20);
        String currentUrl = prefs.getString("server_url", "http://10.21.197.28:3000");
        input.setText(currentUrl);
        input.setSelection(currentUrl.length());
        builder.setView(input);

        builder.setPositiveButton("SAVE", (dialog, which) -> {
            String newUrl = input.getText().toString().trim();
            if (!TextUtils.isEmpty(newUrl)) {
                prefs.edit().putString("server_url", newUrl).apply();
                Toast.makeText(LoginActivity.this, "Server URL updated to: " + newUrl, Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("CANCEL", (dialog, which) -> dialog.cancel());
        builder.show();
    }
}
