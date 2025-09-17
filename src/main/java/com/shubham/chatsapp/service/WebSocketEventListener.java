package com.shubham.chatsapp.service;

import com.shubham.chatsapp.entity.User;
import com.shubham.chatsapp.repository.UserRepository;
import com.shubham.chatsapp.service.WebSocketSessionTracker;
import jakarta.annotation.Nonnull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
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

    @EventListener
    public void handleConnect(@Nonnull SessionConnectEvent event) {
        log.info("🔌 CONNECT EVENT RECEIVED");
        StompHeaderAccessor sha = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = sha.getSessionId();
        String userId = sha.getUser() != null ? sha.getUser().getName() : null;
        log.info("📥 USERID OF USER CONNECTED {}",userId);
        if (userId != null) {
            User byEmail = userRepository.findByEmail(userId).orElseThrow(()-> new IllegalArgumentException("Email from the websocket is not valid"));
            tracker.registerSession(sessionId, byEmail.getId());
            log.info("User {} CONNECTED with session {}", userId, sessionId);
        }
        messageStatusService.markMessagesDelivered(userId);

    }

    @EventListener
    public void handleSubscribe(@Nonnull SessionSubscribeEvent event) {
        log.info("📥 SUBSCRIBE EVENT RECEIVED");
        StompHeaderAccessor sha = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = sha.getSessionId();
        String destination = sha.getDestination();
        String userId = sha.getUser() != null ? sha.getUser().getName() : null;
        log.info("📥 USERID OF USER SUBSCRIBED {}", userId);

        if (destination != null && userId != null) {
            User byEmail = userRepository.findByEmail(userId)
                    .orElseThrow(() -> new IllegalArgumentException("Email from the websocket is not valid"));

            // Handle DIRECT CHAT subscriptions
            if (destination.startsWith("/topic/chat/")) {
                String chatId = destination.substring("/topic/chat/".length());
                log.info("Chatid check {} and user id {} ", chatId, userId);
                tracker.addSubscription(sessionId, byEmail.getId(), UUID.fromString(chatId));
                messageStatusService.markAllMessagesAsRead(UUID.fromString(chatId), byEmail.getId(), false);
            }
            // ✅ ADD GROUP CHAT subscriptions
            else if (destination.startsWith("/topic/group/")) {
                String groupId = destination.substring("/topic/group/".length());
                log.info("Group subscription: groupId={}, userId={}", groupId, userId);

                // Add group subscription tracking
                tracker.addGroupSubscription(sessionId, byEmail.getId(), UUID.fromString(groupId));

                // Mark all unread group messages as read
                messageStatusService.markAllMessagesAsRead(UUID.fromString(groupId), byEmail.getId(), true);
                log.info("Marked all group messages as read for user: {}", userId);
            }
        }
    }

    @EventListener
public void handleDisconnect(@Nonnull SessionDisconnectEvent event) {
    log.info("DISCONNECT EVENT RECEIVED");

        StompHeaderAccessor sha = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = sha.getSessionId();
        String userId = sha.getUser() != null ? sha.getUser().getName() : null;
        User byEmail = userRepository.findByEmail(userId).orElseThrow(()-> new IllegalArgumentException("Email from the websocket is not valid"));
        tracker.removeSession(sessionId,byEmail.getId());
    }

    @EventListener
    public void handleUnsubscribe(@Nonnull SessionUnsubscribeEvent event) {
        StompHeaderAccessor sha = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = sha.getSessionId();
        String userId = sha.getUser() != null ? sha.getUser().getName() : null;

        // Remove subscription from tracker
        if (userId != null) {
            tracker.removeChatSubscriptionsForSession(sessionId);
            log.info("User {} unsubscribed from a chat via session {}", userId, sessionId);
        }
    }



}
