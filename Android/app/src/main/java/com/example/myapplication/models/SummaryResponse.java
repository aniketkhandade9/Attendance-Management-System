package com.example.myapplication.models;

import java.util.List;

public class SummaryResponse {
    private boolean success;
    private List<AttendanceSummary> students;

    public SummaryResponse(boolean success, List<AttendanceSummary> students) {
        this.success = success;
        this.students = students;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public List<AttendanceSummary> getStudents() {
        return students;
    }

    public void setStudents(List<AttendanceSummary> students) {
        this.students = students;
    }
}
