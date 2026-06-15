package com.example.myapplication.models;

import java.util.List;

public class StudentDetailResponse {
    private boolean success;
    private StudentInfo student;
    private List<AttendanceRecord> attendance;

    public StudentDetailResponse(boolean success, StudentInfo student, List<AttendanceRecord> attendance) {
        this.success = success;
        this.student = student;
        this.attendance = attendance;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public StudentInfo getStudent() {
        return student;
    }

    public void setStudent(StudentInfo student) {
        this.student = student;
    }

    public List<AttendanceRecord> getAttendance() {
        return attendance;
    }

    public void setAttendance(List<AttendanceRecord> attendance) {
        this.attendance = attendance;
    }

    public static class StudentInfo {
        private String name;
        private String roll_number;

        public StudentInfo(String name, String rollNumber) {
            this.name = name;
            this.roll_number = rollNumber;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getRollNumber() {
            return roll_number;
        }

        public void setRollNumber(String rollNumber) {
            this.roll_number = rollNumber;
        }
    }
}
