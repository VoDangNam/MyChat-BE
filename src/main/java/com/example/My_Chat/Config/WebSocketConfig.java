package com.example.My_Chat.Config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker // Kích hoạt tính năng xử lý tin nhắn qua WebSocket.
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Cấu hình broker để gửi tin nhắn đến các kênh công cộng (topic) và cá nhân (user).
        config.enableSimpleBroker("/topic", "/user");
        // Đặt prefix cho các endpoint của ứng dụng.
        config.setApplicationDestinationPrefixes("/app");
        config.setUserDestinationPrefix("/user"); // quan trọng
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Đăng ký endpoint kết nối WebSocket với đường dẫn /ws.
        // .withSockJS() giúp tương thích với các trình duyệt cũ.
        registry.addEndpoint("/ws").setAllowedOriginPatterns("*").withSockJS();
    }



}

