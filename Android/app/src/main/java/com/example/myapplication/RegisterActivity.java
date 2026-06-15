package com.example.myapplication;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.models.RegisterRequest;
import com.example.myapplication.models.RegisterResponse;
import com.example.myapplication.network.RetrofitClient;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterActivity extends AppCompatActivity {

    private EditText etName, etUsername, etRollNumber, etPassword;
    private RadioGroup rgRole;
    private RadioButton rbStudent, rbTeacher;
    private LinearLayout layoutRollNumber;
    private Button btnRegister;
    private TextView btnGoToLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        etName = findViewById(R.id.etName);
        etUsername = findViewById(R.id.etRegUsername);
        etRollNumber = findViewById(R.id.etRollNumber);
        etPassword = findViewById(R.id.etRegPassword);
        rgRole = findViewById(R.id.rgRole);
        rbStudent = findViewById(R.id.rbStudent);
        rbTeacher = findViewById(R.id.rbTeacher);
        layoutRollNumber = findViewById(R.id.layoutRollNumber);
        btnRegister = findViewById(R.id.btnRegister);
        btnGoToLogin = findViewById(R.id.btnGoToLogin);

        // Handle role visibility toggle
        rgRole.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rbStudent) {
                layoutRollNumber.setVisibility(View.VISIBLE);
            } else if (checkedId == R.id.rbTeacher) {
                layoutRollNumber.setVisibility(View.GONE);
            }
        });

        btnRegister.setOnClickListener(v -> handleRegistration());
        btnGoToLogin.setOnClickListener(v -> finish()); // Go back to login
    }

    private void handleRegistration() {
        String name = etName.getText().toString().trim();
        String username = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String rollNumber = etRollNumber.getText().toString().trim();

        String role = rbStudent.isChecked() ? "student" : "teacher";

        // Validation checks
        if (TextUtils.isEmpty(name)) {
            etName.setError("Name is required");
            return;
        }
        if (TextUtils.isEmpty(username)) {
            etUsername.setError("Username is required");
            return;
        }
        if ("student".equals(role) && TextUtils.isEmpty(rollNumber)) {
            etRollNumber.setError("Roll number is required");
            return;
        }
        if (TextUtils.isEmpty(password)) {
            etPassword.setError("Password is required");
            return;
        }
        if (password.length() < 6) {
            etPassword.setError("Password must be at least 6 characters");
            return;
        }

        btnRegister.setEnabled(false);
        btnRegister.setText("REGISTERING...");

        RegisterRequest request = new RegisterRequest(username, password, name, role, rollNumber);
        RetrofitClient.getApiService(this).register(request).enqueue(new Callback<RegisterResponse>() {
            @Override
            public void onResponse(@NonNull Call<RegisterResponse> call, @NonNull Response<RegisterResponse> response) {
                btnRegister.setEnabled(true);
                btnRegister.setText("SIGN UP");

                if (response.isSuccessful() && response.body() != null) {
                    RegisterResponse regResponse = response.body();
                    if (regResponse.isSuccess()) {
                        Toast.makeText(RegisterActivity.this, "Registration Successful! Please login.", Toast.LENGTH_LONG).show();
                        finish(); // Redirect back to LoginActivity
                    } else {
                        Toast.makeText(RegisterActivity.this, regResponse.getMessage(), Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(RegisterActivity.this, "Registration failed. Username may be taken.", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<RegisterResponse> call, @NonNull Throwable t) {
                btnRegister.setEnabled(true);
                btnRegister.setText("SIGN UP");
                Toast.makeText(RegisterActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}
