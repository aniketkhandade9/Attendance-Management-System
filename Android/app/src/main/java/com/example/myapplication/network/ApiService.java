package com.example.myapplication.network;

import com.example.myapplication.models.ActiveSessionResponse;
import com.example.myapplication.models.ApiResponse;
import com.example.myapplication.models.CreateSessionRequest;
import com.example.myapplication.models.LoginRequest;
import com.example.myapplication.models.LoginResponse;
import com.example.myapplication.models.RegisterRequest;
import com.example.myapplication.models.RegisterResponse;
import com.example.myapplication.models.StudentAttendanceResponse;
import com.example.myapplication.models.StudentDetailResponse;
import com.example.myapplication.models.SubmitAttendanceRequest;
import com.example.myapplication.models.SummaryResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface ApiService {

    @POST("auth/login")
    Call<LoginResponse> login(@Body LoginRequest request);

    @POST("auth/register")
    Call<RegisterResponse> register(@Body RegisterRequest request);

    @POST("sessions")
    Call<ActiveSessionResponse> createSession(@Body CreateSessionRequest request);

    @GET("sessions/active")
    Call<ActiveSessionResponse> getActiveSession();

    @POST("sessions/{id}/close")
    Call<ApiResponse> closeSession(@Path("id") int sessionId);

    @GET("teacher/attendance-summary")
    Call<SummaryResponse> getAttendanceSummary();

    @GET("teacher/student/{id}")
    Call<StudentDetailResponse> getStudentDetail(@Path("id") int studentId);

    @GET("student/attendance/{studentId}")
    Call<StudentAttendanceResponse> getStudentAttendance(@Path("studentId") int studentId);

    @POST("student/submit-attendance")
    Call<ApiResponse> submitAttendance(@Body SubmitAttendanceRequest request);
}
