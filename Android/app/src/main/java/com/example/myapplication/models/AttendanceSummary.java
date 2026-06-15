package com.example.myapplication.models;

import com.google.gson.annotations.SerializedName;

public class AttendanceSummary {
    private int id;
    private String name;
    
    @SerializedName("roll_number")
    private String rollNumber;
    
    @SerializedName("total_sessions")
    private int totalSessions;
    
    @SerializedName("attended_sessions")
    private int attendedSessions;
    
    @SerializedName("attendance_percentage")
    private double attendancePercentage;

    public AttendanceSummary(int id, String name, String rollNumber, int totalSessions, int attendedSessions, double attendancePercentage) {
        this.id = id;
        this.name = name;
        this.rollNumber = rollNumber;
        this.totalSessions = totalSessions;
        this.attendedSessions = attendedSessions;
        this.attendancePercentage = attendancePercentage;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRollNumber() {
        return rollNumber;
    }

    public void setRollNumber(String rollNumber) {
        this.rollNumber = rollNumber;
    }

    public int getTotalSessions() {
        return totalSessions;
    }

    public void setTotalSessions(int totalSessions) {
        this.totalSessions = totalSessions;
    }

    public int getAttendedSessions() {
        return attendedSessions;
    }

    public void setAttendedSessions(int attendedSessions) {
        this.attendedSessions = attendedSessions;
    }

    public double getAttendancePercentage() {
        return attendancePercentage;
    }

    public void setAttendancePercentage(double attendancePercentage) {
        this.attendancePercentage = attendancePercentage;
    }
}
