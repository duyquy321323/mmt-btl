package com.mmt.btl.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(@SuppressWarnings("null") CorsRegistry registry) {
        registry.addMapping("/**") // Áp dụng cho tất cả các endpoint
                .allowedOrigins("http://localhost:3000", "http://localhost:3001") // Chỉ định domain được phép truy cập
                                                         // (thay bằng domain của
                // bạn)
                .allowedMethods("GET", "POST", "PUT", "DELETE") // Chỉ định các phương thức được phép
                .allowedHeaders("*") // Cho phép tất cả các header
                .allowCredentials(true); // Cho phép gửi thông tin xác thực (cookie)
    }
}