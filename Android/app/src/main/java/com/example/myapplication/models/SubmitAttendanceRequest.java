package com.example.myapplication.models;

import com.google.gson.annotations.SerializedName;

public class SubmitAttendanceRequest {
    @SerializedName("studentId")
    private int studentId;
    
    @SerializedName("session_code")
    private String sessionCode;

    public SubmitAttendanceRequest(int studentId, String sessionCode) {
        this.studentId = studentId;
        this.sessionCode = sessionCode;
    }

    public int getStudentId() {
        return studentId;
    }

    public void setStudentId(int studentId) {
        this.studentId = studentId;
    }

    public String getSessionCode() {
        return sessionCode;
    }

    public void setSessionCode(String sessionCode) {
        this.sessionCode = sessionCode;
    }
}
