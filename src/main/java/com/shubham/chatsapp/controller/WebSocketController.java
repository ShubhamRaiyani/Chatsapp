package com.shubham.chatsapp.controller;

import com.shubham.chatsapp.dto.MessageDTO;
import com.shubham.chatsapp.entity.Message;
import com.shubham.chatsapp.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
public class WebSocketController {

    private final SimpMessagingTemplate messagingTemplate;
    private final MessageService messageService;

    public WebSocketController(SimpMessagingTemplate messagingTemplate, MessageService messageService) {
        this.messagingTemplate = messagingTemplate;
        this.messageService = messageService;
    }

    @MessageMapping("/chat.send") // From client to /app/chat.send
    public void handleSendMessage(@Payload MessageDTO messageDTO) {
        // Save to DB
        System.out.println(">>>>> handleSendMessage called");
        MessageDTO savedMessage = messageService.sendMessage(messageDTO);

        // Broadcast to all clients subscribed to the chat
        if (messageDTO.getChatId() != null) {
            messagingTemplate.convertAndSend("/topic/chat/" + messageDTO.getChatId(), savedMessage);
        } else if (messageDTO.getGroupId() != null) {
            messagingTemplate.convertAndSend("/topic/group/" + messageDTO.getGroupId(), savedMessage);
        }
    }

}
