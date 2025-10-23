package com.example.My_Chat.DTO;

import lombok.Data;

@Data
public class LoginResponse {
    private String username;
    private String role;
    private Long id;
    private String email;
    private String token;
    public LoginResponse(String username, String role, String token) {
        this.username = username;
        this.role = role;
        this.token = token;
    }

}
