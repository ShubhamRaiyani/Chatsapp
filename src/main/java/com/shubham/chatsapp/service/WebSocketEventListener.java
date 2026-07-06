package com.shubham.chatsapp.service;

import com.shubham.chatsapp.dto.PresenceEventDTO;
import com.shubham.chatsapp.entity.User;
import com.shubham.chatsapp.repository.UserRepository;
import com.shubham.chatsapp.service.WebSocketSessionTracker;
import jakarta.annotation.Nonnull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.*;

import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class WebSocketEventListener {

    private final WebSocketSessionTracker tracker;
    private final MessageStatusService  messageStatusService;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate;

    @EventListener
    public void handleConnect(@Nonnull SessionConnectEvent event) {
        StompHeaderAccessor sha = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = sha.getSessionId();
        String userId = sha.getUser() != null ? sha.getUser().getName() : null;
        if (userId != null) {
            User byEmail = userRepository.findByEmail(userId).orElseThrow(() -> new IllegalArgumentException("Email from the websocket is not valid"));
            tracker.registerSession(sessionId, byEmail.getId(), userId);
            log.info("User {} connected", userId);

            PresenceEventDTO presence = new PresenceEventDTO();
            presence.setType("PRESENCE");
            presence.setUserId(userId);
            presence.setOnline(true);
            messagingTemplate.convertAndSend("/topic/presence", presence);
        }
        messageStatusService.markMessagesDelivered(userId);
    }

    @EventListener
    public void handleSubscribe(@Nonnull SessionSubscribeEvent event) {
        StompHeaderAccessor sha = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = sha.getSessionId();
        String destination = sha.getDestination();
        String userId = sha.getUser() != null ? sha.getUser().getName() : null;

        if (destination != null && userId != null) {
            User byEmail = userRepository.findByEmail(userId)
                    .orElseThrow(() -> new IllegalArgumentException("Email from the websocket is not valid"));

            if (destination.startsWith("/topic/chat/")) {
                String chatId = destination.substring("/topic/chat/".length());
                tracker.addSubscription(sessionId, byEmail.getId(), UUID.fromString(chatId));
                messageStatusService.markAllMessagesAsRead(UUID.fromString(chatId), byEmail.getId(), false);
            } else if (destination.startsWith("/topic/group/")) {
                String groupId = destination.substring("/topic/group/".length());
                tracker.addGroupSubscription(sessionId, byEmail.getId(), UUID.fromString(groupId));
                messageStatusService.markAllMessagesAsRead(UUID.fromString(groupId), byEmail.getId(), true);
            }
        }
    }

    @EventListener
    public void handleDisconnect(@Nonnull SessionDisconnectEvent event) {
        StompHeaderAccessor sha = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = sha.getSessionId();
        String userId = sha.getUser() != null ? sha.getUser().getName() : null;
        if (userId == null) return;

        User byEmail = userRepository.findByEmail(userId)
                .orElseThrow(() -> new IllegalArgumentException("Email from the websocket is not valid"));
        tracker.removeSession(sessionId, byEmail.getId());

        log.info("User {} disconnected", userId);
        if (!tracker.isUserConnected(byEmail.getId())) {
            PresenceEventDTO presence = new PresenceEventDTO();
            presence.setType("PRESENCE");
            presence.setUserId(userId);
            presence.setOnline(false);
            messagingTemplate.convertAndSend("/topic/presence", presence);
        }
    }

    @EventListener
    public void handleUnsubscribe(@Nonnull SessionUnsubscribeEvent event) {
        StompHeaderAccessor sha = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = sha.getSessionId();
        String userId = sha.getUser() != null ? sha.getUser().getName() : null;

        // Remove subscription from tracker
        if (userId != null) {
            tracker.removeChatSubscriptionsForSession(sessionId);
        }
    }



}
