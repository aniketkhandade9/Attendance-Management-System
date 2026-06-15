package com.example.myapplication.models;

import java.util.List;

public class StudentAttendanceResponse {
    private boolean success;
    private AttendanceSummaryInfo summary;
    private List<AttendanceRecord> history;

    public StudentAttendanceResponse(boolean success, AttendanceSummaryInfo summary, List<AttendanceRecord> history) {
        this.success = success;
        this.summary = summary;
        this.history = history;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public AttendanceSummaryInfo getSummary() {
        return summary;
    }

    public void setSummary(AttendanceSummaryInfo summary) {
        this.summary = summary;
    }

    public List<AttendanceRecord> getHistory() {
        return history;
    }

    public void setHistory(List<AttendanceRecord> history) {
        this.history = history;
    }

    public static class AttendanceSummaryInfo {
        private int total_sessions;
        private int attended_sessions;
        private double attendance_percentage;

        public AttendanceSummaryInfo(int totalSessions, int attendedSessions, double attendancePercentage) {
            this.total_sessions = totalSessions;
            this.attended_sessions = attendedSessions;
            this.attendance_percentage = attendancePercentage;
        }

        public int getTotalSessions() {
            return total_sessions;
        }

        public void setTotalSessions(int totalSessions) {
            this.total_sessions = totalSessions;
        }

        public int getAttendedSessions() {
            return attended_sessions;
        }

        public void setAttendedSessions(int attendedSessions) {
            this.attended_sessions = attendedSessions;
        }

        public double getAttendancePercentage() {
            return attendance_percentage;
        }

        public void setAttendancePercentage(double attendancePercentage) {
            this.attendance_percentage = attendancePercentage;
        }
    }
}
