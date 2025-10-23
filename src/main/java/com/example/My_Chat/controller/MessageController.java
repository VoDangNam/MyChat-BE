package com.example.My_Chat.controller;

import com.example.My_Chat.model.Message;
import com.example.My_Chat.repository.MessageReponsitory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
@RestController
public class MessageController {

    @Autowired
    private SimpMessagingTemplate simpMessagingTemplate; // Dùng để gửi tin nhắn đến user cụ thể.

    @Autowired
    private MessageReponsitory messageReponsitory;

    @MessageMapping("chat.privateMessage")// Endpoint nhận tin nhắn riêng tư
    public void sendPrivateMessage(Message mesaage){
        simpMessagingTemplate.convertAndSendToUser(
                mesaage.getRecipientName(),
                "/queue/message",
                mesaage
        );

        //Lưu thông tin người nhận
        messageReponsitory.save(mesaage);
    }

    @GetMapping("/messages")// API REST để lấy lịch sử chat.
    public List<Message> listChatHistory(@RequestParam String user1, String user2){
        // Tìm tin nhắn giữa 2 người dùng.
        return messageReponsitory.findBySenderNameAndRecipientNameOrRecipientNameAndSenderName(user1, user2, user1, user2);
    }

}
