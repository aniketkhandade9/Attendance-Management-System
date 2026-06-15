package com.example.myapplication.models;

import com.google.gson.annotations.SerializedName;

public class RegisterRequest {
    private String username;
    private String password;
    private String name;
    private String role;
    
    @SerializedName("roll_number")
    private String rollNumber;

    public RegisterRequest(String username, String password, String name, String role, String rollNumber) {
        this.username = username;
        this.password = password;
        this.name = name;
        this.role = role;
        this.rollNumber = rollNumber;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getRollNumber() {
        return rollNumber;
    }

    public void setRollNumber(String rollNumber) {
        this.rollNumber = rollNumber;
    }
}
