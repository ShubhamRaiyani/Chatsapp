package com.shubham.chatsapp.controller;

import com.shubham.chatsapp.dto.MessageDTO;
import com.shubham.chatsapp.dto.ReadReceiptDTO;
import com.shubham.chatsapp.entity.Message;
import com.shubham.chatsapp.entity.User;
import com.shubham.chatsapp.repository.UserRepository;
import com.shubham.chatsapp.service.MessageService;
import com.shubham.chatsapp.service.MessageStatusService;
import com.shubham.chatsapp.service.WebSocketSessionTracker;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;

import java.security.Principal;
import java.util.Optional;
import java.util.UUID;

@Controller
public class WebSocketController {

    private final SimpMessagingTemplate messagingTemplate;
    private final MessageService messageService;
    private final StringRedisTemplate redisTemplate;
    private final UserRepository userRepository;
    private final WebSocketSessionTracker sessionTracker;
    private final MessageStatusService messageStatusService;
    public WebSocketController(SimpMessagingTemplate messagingTemplate, MessageService messageService, StringRedisTemplate redisTemplate, UserRepository userRepository, WebSocketSessionTracker sessionTracker, MessageStatusService messageStatusService) {
        this.messagingTemplate = messagingTemplate;
        this.messageService = messageService;
        this.redisTemplate = redisTemplate;
        this.userRepository = userRepository;
        this.sessionTracker = sessionTracker;
        this.messageStatusService = messageStatusService;
    }


    @MessageMapping("/chat.send") // From client to /app/chat.send
    public void handleSendMessage(@Payload MessageDTO messageDTO,Authentication authentication) {
        String authenticatedEmail = authentication.getName();
//
//        // Optionally, compare and enforce
//        if (!authenticatedEmail.equals(messageDTO.getSenderEmail())) {
//            throw new SecurityException("Sender spoofing attempt detected!");
//        }

        // Save to DB
        System.out.println(">>>>> handleSendMessage called");
        MessageDTO savedMessageDTO = messageService.sendMessage(messageDTO,authenticatedEmail);
        Message savedMessage = messageService.getMessageFromMessageDTO(savedMessageDTO);

        User receiverUser = userRepository.findByEmail(messageDTO.getReceiverEmail()).orElseThrow(() -> new UsernameNotFoundException("User not found"));;
        String receiverUserId = receiverUser.getId().toString();
        UUID uuidRecieverUserId = UUID.fromString(receiverUserId);


        // Broadcast to all clients subscribed to the chat
        if (messageDTO.getChatId() != null) {
            messagingTemplate.convertAndSend("/topic/chat/" + messageDTO.getChatId(), savedMessageDTO);

             // ✅ Check if recipient is subscribed to this chat
        if (sessionTracker.isUserConnected(uuidRecieverUserId)) {
            if (sessionTracker.isUserSubscribedToChat(uuidRecieverUserId, savedMessage.getChat().getId())) {
                messageStatusService.markRead(savedMessage, receiverUser);
            } else {
                messageStatusService.markDelivered(savedMessage,receiverUser);
            }
        }
        boolean a = sessionTracker.isUserConnected(uuidRecieverUserId);
            System.out.println("result o conection checkof reciever " + a);
            boolean b = sessionTracker.isUserSubscribedToChat(uuidRecieverUserId,savedMessage.getChat().getId());
            System.out.println("result o subcriber checkof reciever " + b);
//            if (sessionTracker.isUserSubscribedToChat(String.valueOf(receiverUser.getId()), messageDTO.getChatId().toString())) {
//                messageStatusService.markDelivered(savedMessage, receiverUser);
//            }
        } else if (messageDTO.getGroupId() != null) {
            messagingTemplate.convertAndSend("/topic/group/" + messageDTO.getGroupId(), savedMessageDTO);
        } else {
            System.out.println("Responseentity<ChatNotAvailable>");
        }
    }





//
//
//    @MessageMapping("/chat.read") // client sends to /app/chat.read
//    public void handleReadMessages(@Payload ReadReceiptDTO payload, Principal principal) {
//        String userId = principal.getName();
//        messageStatusService.markAllMessagesAsRead(payload.getChatId(), userId);
//    }


}
