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

        System.out.println(">>> handleSendMessage called for: " +
                (messageDTO.getChatId() != null ? "Direct Chat" : "Group Chat"));

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

        // Handle receiver status
        User receiverUser = savedMessage.getReceiver();
        UUID receiverUserId = receiverUser.getId();

        if (sessionTracker.isUserConnected(receiverUserId)) {
            if (sessionTracker.isUserSubscribedToChat(receiverUserId, savedMessage.getChat().getId())) {
                messageStatusService.markRead(savedMessage, receiverUser);
                System.out.println("Message marked as READ for receiver: " + receiverUser.getEmail());
            } else {
                messageStatusService.markDelivered(savedMessage, receiverUser);
                System.out.println("Message marked as DELIVERED for receiver: " + receiverUser.getEmail());
            }
        } else {
            System.out.println("Receiver is offline, status remains SENT");
        }
    }

    private void handleGroupMessage(MessageDTO savedMessageDTO, Message savedMessage) {
        // Broadcast to group topic
        messagingTemplate.convertAndSend("/topic/group/" + savedMessageDTO.getGroupId(), savedMessageDTO);

        // Get all group members except sender
        List<User> groupMembers = messageService.getGroupMembers(savedMessage.getGroup().getId());
        User sender = savedMessage.getSender();

        System.out.println("Processing group message for " + groupMembers.size() + " members");

        // Handle status for each group member
        for (User member : groupMembers) {
            if (!member.getId().equals(sender.getId())) { // Skip sender
                UUID memberId = member.getId();

                if (sessionTracker.isUserConnected(memberId)) {
                    // ✅ FIXED - Check if user is subscribed to this specific group
                    if (sessionTracker.isUserSubscribedToGroup(memberId, savedMessage.getGroup().getId())) {
                        messageStatusService.markRead(savedMessage, member);
                        System.out.println("Group message marked as READ for: " + member.getEmail());
                    } else {
                        messageStatusService.markDelivered(savedMessage, member);
                        System.out.println("Group message marked as delivered for: " + member.getEmail());
                    }
                } else {
                    System.out.println("Group member offline: " + member.getEmail());
                    // Status remains SENT for offline users
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


