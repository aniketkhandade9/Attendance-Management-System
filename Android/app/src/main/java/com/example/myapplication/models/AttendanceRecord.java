package com.example.myapplication.models;

import com.google.gson.annotations.SerializedName;

public class AttendanceRecord {
    @SerializedName("session_id")
    private int sessionId;
    
    private String date;
    
    @SerializedName("session_code")
    private String sessionCode;
    
    private String status;
    
    @SerializedName("submitted_at")
    private String submittedAt;

    public AttendanceRecord(int sessionId, String date, String sessionCode, String status, String submittedAt) {
        this.sessionId = sessionId;
        this.date = date;
        this.sessionCode = sessionCode;
        this.status = status;
        this.submittedAt = submittedAt;
    }

    public int getSessionId() {
        return sessionId;
    }

    public void setSessionId(int sessionId) {
        this.sessionId = sessionId;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getSessionCode() {
        return sessionCode;
    }

    public void setSessionCode(String sessionCode) {
        this.sessionCode = sessionCode;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getSubmittedAt() {
        return submittedAt;
    }

    public void setSubmittedAt(String submittedAt) {
        this.submittedAt = submittedAt;
    }
}
