package com.example.My_Chat.DTO;

import lombok.Data;

@Data
public class FriendDTO {
    private Long id;
    private String status;
    // Chỉ chứa thông tin cần thiết của User, không chứa Entity User đầy đủ
    private UserDTO user;
    private UserDTO friend;

    // Constructor, getters, setters...
}