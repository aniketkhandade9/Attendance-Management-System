package com.example.myapplication.models;

import com.google.gson.annotations.SerializedName;

public class Session {
    private int id;
    private String date;
    
    @SerializedName("session_code")
    private String sessionCode;
    private String status;

    public Session(int id, String date, String sessionCode, String status) {
        this.id = id;
        this.date = date;
        this.sessionCode = sessionCode;
        this.status = status;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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
}
