package com.example.My_Chat.Config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    // CHỈ CẦN 1 HÀM NÀY LÀ ĐỦ
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**") // <-- SỬA THÀNH /** ĐỂ CHO PHÉP TẤT CẢ API
                .allowedOrigins(
                        "https://my-frontend-rlt3.onrender.com", // Cho phép app "Live"
                        "http://localhost:5173"                 // Cho phép app "local"
                )
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true);
    }
}