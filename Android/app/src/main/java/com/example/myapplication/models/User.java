package com.example.myapplication.models;

import com.google.gson.annotations.SerializedName;

public class User {
    private int id;
    private String username;
    private String name;
    private String role;
    
    @SerializedName("roll_number")
    private String rollNumber;

    public User(int id, String username, String name, String role, String rollNumber) {
        this.id = id;
        this.username = username;
        this.name = name;
        this.role = role;
        this.rollNumber = rollNumber;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
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
