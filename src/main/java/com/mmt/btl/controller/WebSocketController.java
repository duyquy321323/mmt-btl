package com.mmt.btl.controller;

import org.springframework.messaging.MessagingException;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mmt.btl.response.LogServerResponse;
import com.mmt.btl.response.LogTrackerResponse;

@RestController
public class WebSocketController {

    private final SimpMessagingTemplate messagingTemplate;

    private final ObjectMapper mapper = new ObjectMapper();

    public WebSocketController(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @MessageMapping("/sendMessageServer") // Nhận tin nhắn từ client tại endpoint /app/sendMessage
    @SendTo("/topic/server/{peerId}") // Gửi tin nhắn đến các client qua kênh /server
    public String processMessageServerFromClient(String message, String peerId) {
        return "Server received: " + message;
    }

    // Gửi tin nhắn đến tất cả các client từ server
    // hàm cho phép gửi tin chủ động mà không cần nhận tin từ client trước
    public void sendMessageServerToClients(String message, String username, String userAgent) throws MessagingException, JsonProcessingException {
        LogServerResponse response = new LogServerResponse();
        response.setUserAgent(userAgent);
        response.setUsername(username);
        response.setMessage(message);
        messagingTemplate.convertAndSend("/topic/server", mapper.writeValueAsString(response));
    }

    // Gửi tin nhắn đến tất cả các client từ tracker
    // hàm cho phép gửi tin chủ động mà không cần nhận tin từ client trước
    public void sendMessageTrackerToClients(String message, Long trackerPort)
            throws MessagingException, JsonProcessingException {
        LogTrackerResponse response = new LogTrackerResponse();
        response.setMessage(message);
        response.setPort(trackerPort);
        messagingTemplate.convertAndSend("/topic/tracker/" + trackerPort.toString(),
                mapper.writeValueAsString(response));
    }
}
