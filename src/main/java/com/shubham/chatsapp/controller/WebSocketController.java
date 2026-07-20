package com.shubham.chatsapp.controller;

import com.shubham.chatsapp.dto.MessageDTO;
import com.shubham.chatsapp.dto.ReadReceiptDTO;
import com.shubham.chatsapp.dto.TypingEventDTO;
import com.shubham.chatsapp.dto.WebRTCSignalDTO;
import com.shubham.chatsapp.entity.Message;
import com.shubham.chatsapp.entity.User;
import com.shubham.chatsapp.repository.UserRepository;
import com.shubham.chatsapp.service.MessageService;
import com.shubham.chatsapp.service.MessageStatusService;
import com.shubham.chatsapp.service.WebSocketSessionTracker;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Controller;

import java.util.List;
import java.util.UUID;

@Controller
@RequiredArgsConstructor
public class WebSocketController {

    private final SimpMessagingTemplate messagingTemplate;
    private final MessageService messageService;
    private final UserRepository userRepository;
    private final WebSocketSessionTracker sessionTracker;
    private final MessageStatusService messageStatusService;

    @MessageMapping("/chat.send") // Single entry point for both direct and group messages
    public void handleSendMessage(@Payload MessageDTO messageDTO, Authentication authentication) {
        String authenticatedEmail = authentication.getName();

        // Save message to database
        MessageDTO savedMessageDTO = messageService.sendMessage(messageDTO, authenticatedEmail);
        Message savedMessage = messageService.getMessageFromMessageDTO(savedMessageDTO);

        // Route based on message type
        if (messageDTO.getChatId() != null) {
            handleDirectMessage(savedMessageDTO, savedMessage);
        } else if (messageDTO.getGroupId() != null) {
            handleGroupMessage(savedMessageDTO, savedMessage);
        } else {
            throw new IllegalArgumentException("Either chatId or groupId must be provided");
        }
    }

    private void handleDirectMessage(MessageDTO savedMessageDTO, Message savedMessage) {
        // Broadcast to chat topic
        messagingTemplate.convertAndSend("/topic/chat/" + savedMessageDTO.getChatId(), savedMessageDTO);

        // Push notification to receiver's personal queue so their sidebar updates even when they have another chat open
        User receiverUser = savedMessage.getReceiver();
        UUID receiverUserId = receiverUser.getId();
        messagingTemplate.convertAndSendToUser(receiverUser.getEmail(), "/queue/notifications", savedMessageDTO);

        if (sessionTracker.isUserConnected(receiverUserId)) {
            if (sessionTracker.isUserSubscribedToChat(receiverUserId, savedMessage.getChat().getId())) {
                messageStatusService.markRead(savedMessage, receiverUser);
            } else {
                messageStatusService.markDelivered(savedMessage, receiverUser);
            }
        }
    }

    @MessageMapping("/call.signal")
    public void handleCallSignal(@Payload WebRTCSignalDTO signal, Authentication authentication) {
        // Always enforce identity server-side — never trust fromEmail from the client
        signal.setFromEmail(authentication.getName());
        messagingTemplate.convertAndSendToUser(signal.getToEmail(), "/queue/call", signal);
    }

    @MessageMapping("/typing")
    public void handleTyping(@Payload TypingEventDTO event, Authentication authentication) {
        event.setType("TYPING");
        event.setUserId(authentication.getName()); // enforce server-side identity

        if (event.getChatId() != null && !event.getChatId().isBlank()) {
            messagingTemplate.convertAndSend("/topic/chat/" + event.getChatId(), event);
        } else if (event.getGroupId() != null && !event.getGroupId().isBlank()) {
            messagingTemplate.convertAndSend("/topic/group/" + event.getGroupId(), event);
        }
    }

    private void handleGroupMessage(MessageDTO savedMessageDTO, Message savedMessage) {
        // Broadcast to group topic
        messagingTemplate.convertAndSend("/topic/group/" + savedMessageDTO.getGroupId(), savedMessageDTO);

        // Get all group members except sender
        List<User> groupMembers = messageService.getGroupMembers(savedMessage.getGroup().getId());
        User sender = savedMessage.getSender();

        for (User member : groupMembers) {
            if (!member.getId().equals(sender.getId())) {
                UUID memberId = member.getId();
                messagingTemplate.convertAndSendToUser(member.getEmail(), "/queue/notifications", savedMessageDTO);

                if (sessionTracker.isUserConnected(memberId)) {
                    if (sessionTracker.isUserSubscribedToGroup(memberId, savedMessage.getGroup().getId())) {
                        messageStatusService.markRead(savedMessage, member);
                    } else {
                        messageStatusService.markDelivered(savedMessage, member);
                    }
                }
            }
        }
    }


//    @MessageMapping("/chat.read") // For both direct and group read receipts
//    public void handleReadMessages(@Payload ReadReceiptDTO payload, Authentication authentication) {
//        String userEmail = authentication.getName();
//        User user = userRepository.findByEmail(userEmail)
//                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
//
//        if (payload.getChatId() != null) {
//            // Direct chat read receipt
//            messageStatusService.markAllMessagesAsRead(payload.getChatId(), user.getId(), false);
//            System.out.println("Marked all direct messages as read for chat: " + payload.getChatId());
//        } else if (payload.getGroupId() != null) {
//            // Group chat read receipt
//            messageStatusService.markAllMessagesAsRead(payload.getGroupId(), user.getId(), true);
//            System.out.println("Marked all group messages as read for group: " + payload.getGroupId());
//        }
//    }
}

//
//
//    @MessageMapping("/chat.read") // client sends to /app/chat.read
//    public void handleReadMessages(@Payload ReadReceiptDTO payload, Principal principal) {
//        String userId = principal.getName();
//        messageStatusService.markAllMessagesAsRead(payload.getChatId(), userId);
//    }


