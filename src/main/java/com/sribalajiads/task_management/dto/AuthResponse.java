package com.sribalajiads.task_management.dto;

public class AuthResponse {
    private String token;
    private UserResponseDTO user; // Nested User Details

    // Constructor
    public AuthResponse(String token, UserResponseDTO user) {
        this.token = token;
        this.user = user;
    }

    // Getters and Setters
    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public UserResponseDTO getUser() { return user; }
    public void setUser(UserResponseDTO user) { this.user = user; }
}