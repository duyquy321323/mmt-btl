package com.mmt.btl.controller;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class WebSocketController {

    private final SimpMessagingTemplate messagingTemplate;

    public WebSocketController(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @MessageMapping("/sendMessage") // Nhận tin nhắn từ client tại endpoint /app/sendMessage
    @SendTo("/topic/messages") // Gửi tin nhắn đến các client qua kênh /topic/messages
    public String processMessageFromClient(String message) {
        return "Server received: " + message;
    }

    // Gửi tin nhắn đến tất cả các client từ server
    // hàm cho phép gửi tin chủ động mà không cần nhận tin từ client trước
    public void sendMessageToClients(String message) {
        messagingTemplate.convertAndSend("/topic/messages", message);
    }
}
