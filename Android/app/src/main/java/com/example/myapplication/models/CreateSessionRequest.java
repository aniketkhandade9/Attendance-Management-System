package com.example.myapplication.models;

import com.google.gson.annotations.SerializedName;

public class CreateSessionRequest {
    private String date;
    
    @SerializedName("session_code")
    private String sessionCode;

    public CreateSessionRequest(String date, String sessionCode) {
        this.date = date;
        this.sessionCode = sessionCode;
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
}
