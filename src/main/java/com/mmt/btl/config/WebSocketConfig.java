package com.mmt.btl.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.server.support.HttpSessionHandshakeInterceptor;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(@SuppressWarnings("null") MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic"); // Kênh để server gửi tin nhắn đến client
        config.setApplicationDestinationPrefixes("/app"); // Tiền tố cho các tin nhắn gửi từ client
    }

    @Override
    public void registerStompEndpoints(@SuppressWarnings("null") StompEndpointRegistry registry) {
        registry.addEndpoint("/ws").setAllowedOrigins("http://localhost:3000")
                .addInterceptors(new HttpSessionHandshakeInterceptor()).withSockJS(); // Endpoint cho client kết nối
                                                                                      // WebSocket
    }
}