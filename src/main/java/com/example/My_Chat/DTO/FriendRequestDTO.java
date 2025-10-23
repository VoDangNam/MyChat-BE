package com.example.My_Chat.DTO;

import lombok.Data;

@Data
public class FriendRequestDTO {
    private Long requestId; // id của bản ghi trong bảng friends
    private String fromUsername;
    private String avatar;
}
