package com.example.My_Chat.repository;

import com.example.My_Chat.model.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageReponsitory extends JpaRepository<Message, Long> {
    // Phương thức đặc biệt của Spring Data JPA để lấy tin nhắn giữa 2 người dùng.
    // Lấy tin nhắn từ sender1 đến recipient1 HOẶC từ recipient2 đến sender2.
    List<Message> findBySenderNameAndRecipientNameOrRecipientNameAndSenderName(String sender1, String recipient1, String sender2, String recipient2);
}