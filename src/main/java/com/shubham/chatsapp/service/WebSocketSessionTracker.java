package com.shubham.chatsapp.service;

import com.shubham.chatsapp.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Slf4j
public class WebSocketSessionTracker {

    private final UserRepository userRepository;

    // sessionId => userId
//    private static  Map<String, String> sessionUserMap = new ConcurrentHashMap<>();
//
//    // userId => Set of sessionIds (multiple tabs/devices)
//    private static   Map<String, Set<String>> userSessionMap = new ConcurrentHashMap<>();
//
//    // sessionId => Set of chatIds
//    private static  Map<String, Set<String>> sessionChatMap = new ConcurrentHashMap<>();
//
//    // userId => connected (for online/offline status)
//    private static  Set<String> connectedUsers = ConcurrentHashMap.newKeySet();

    public WebSocketSessionTracker(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Autowired
    private StringRedisTemplate redisTemplate;

    private static final String ONLINE_USER_KEY = "connectedUsers";
    private static final String ONLINE_EMAIL_KEY = "connectedEmails";

    public void registerSession(String sessionId, UUID userId, String email) {
        redisTemplate.opsForSet().add(ONLINE_USER_KEY, userId.toString());
        redisTemplate.opsForSet().add(ONLINE_EMAIL_KEY, email);
        redisTemplate.opsForValue().set("session:" + sessionId, userId.toString());
        redisTemplate.opsForValue().set("sessionEmail:" + sessionId, email);
        redisTemplate.opsForSet().add("session:all", sessionId);
    }

    public Set<String> getAllOnlineEmails() {
        Set<String> members = redisTemplate.opsForSet().members(ONLINE_EMAIL_KEY);
        return members != null ? members : new java.util.HashSet<>();
    }

    public void removeSession(String sessionId, UUID userId) {
        try {
            String redisUserId = redisTemplate.opsForValue().get("session:" + sessionId);
            String email = redisTemplate.opsForValue().get("sessionEmail:" + sessionId);

            if (redisUserId != null) {
                redisTemplate.delete("session:" + sessionId);
                redisTemplate.delete("sessionEmail:" + sessionId);
                redisTemplate.opsForSet().remove("session:all", sessionId);

                Set<String> allSessions = redisTemplate.opsForSet().members("session:all");

                if (allSessions != null) {
                    boolean stillOnline = allSessions.stream().anyMatch(k -> {
                        try {
                            String storedUserId = redisTemplate.opsForValue().get("session:" + k);
                            return storedUserId != null && storedUserId.equals(userId.toString());
                        } catch (Exception e) {
                            log.error("Redis error reading session key: session:{}", k, e);
                            return false;
                        }
                    });

                    if (!stillOnline) {
                        redisTemplate.opsForSet().remove("connectedUsers", userId.toString());
                        if (email != null) {
                            redisTemplate.opsForSet().remove(ONLINE_EMAIL_KEY, email);
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error("Failed to remove session for {}: {}", sessionId, e.getMessage(), e);
        }
    }


    public void addSubscription(String sessionId, UUID userId, UUID chatId) {
        redisTemplate.opsForSet().add("user:" + userId + ":chats", chatId.toString());
        redisTemplate.opsForSet().add("session:" + sessionId + ":chats", chatId.toString());

    }
    public void removeChatSubscriptionsForSession(String sessionId) {
        String userId = redisTemplate.opsForValue().get("session:" + sessionId);

        if (userId != null) {
            // Remove CHAT subscriptions
            String sessionChatsKey = "session:" + sessionId + ":chats";
            Set<String> chatIds = redisTemplate.opsForSet().members(sessionChatsKey);
            if (chatIds != null) {
                for (String chatId : chatIds) {
                    redisTemplate.opsForSet().remove("user:" + userId + ":chats", chatId);
                }
            }
            redisTemplate.delete(sessionChatsKey);

            // ✅ ADD GROUP subscriptions cleanup
            String sessionGroupsKey = "session:" + sessionId + ":groups";
            Set<String> groupIds = redisTemplate.opsForSet().members(sessionGroupsKey);
            if (groupIds != null) {
                for (String groupId : groupIds) {
                    redisTemplate.opsForSet().remove("user:" + userId + ":groups", groupId);
                }
            }
            redisTemplate.delete(sessionGroupsKey);
        }
    }

    public boolean isUserConnected(UUID userId) {
        return Boolean.TRUE.equals(redisTemplate.opsForSet().isMember(ONLINE_USER_KEY, String.valueOf(userId)));
    }

    public boolean isUserSubscribedToChat(UUID userId, UUID chatId) {
        return Boolean.TRUE.equals(redisTemplate.opsForSet().isMember(
                "user:" + userId + ":chats", String.valueOf(chatId)));
    }

    public boolean isUserSubscribedToGroup(UUID userId, UUID groupId) {
        return Boolean.TRUE.equals(redisTemplate.opsForSet().isMember(
                "user:" + userId + ":groups", groupId.toString()));
    }

    public void addGroupSubscription(String sessionId, UUID userId, UUID groupId) {
        redisTemplate.opsForSet().add("user:" + userId + ":groups", groupId.toString());
        redisTemplate.opsForSet().add("session:" + sessionId + ":groups", groupId.toString());
    }


// You'll also need to modify the WebSocketEventListener to handle group subscriptions


//     🔄 Add new WebSocket sessiond
//    public void registerSession(String sessionId, String userId) { // userid = email from sha
//        sessionUserMap.put(sessionId, userId);
//        log.info("register sessionmethod add hashmap {}", sessionUserMap.get(sessionId));
//        userSessionMap.computeIfAbsent(userId, k -> ConcurrentHashMap.newKeySet()).add(sessionId);
//        connectedUsers.add(userId);
//        log.info("added {} to onlineusers", userId);
//    }

//    //  Remove session on disconnect
//    public void removeSession(String sessionId) {
//        String userId = sessionUserMap.remove(sessionId);
//        if (userId != null) {
//            // Remove sessionId from userSessionMap
//            Set<String> sessions = userSessionMap.get(userId);
//            if (sessions != null) {
//                sessions.remove(sessionId);
//                if (sessions.isEmpty()) {
//                    userSessionMap.remove(userId);
//                    connectedUsers.remove(userId);
//                    log.info("User {} is now OFFLINE", userId);
//                }
//            }
//            // Clean up chat subscriptions
//            sessionChatMap.remove(sessionId);
//        }
//    }
//    public void removeChatSubscriptionsForSession(String sessionId) {
//        sessionChatMap.remove(sessionId); // remove all chatIds for this session
//    }

    // Check if user is online (has any active session)
//    public boolean isUserConnected(String userId) {
//        log.info("Checking is user connected to websocket {}", userId);
//        User user = userRepository.findById(UUID.fromString(userId)).orElseThrow(()->new IllegalArgumentException("User(reciever) not valid from frontend"));
//
//        return connectedUsers.contains(user.getEmail());
//    }
    // ✅ Check if user is subscribed to a chat in any of their sessions
//    public boolean isUserSubscribedToChat(String userId, String chatId) {
//        User user = userRepository.findById(UUID.fromString(userId)).orElseThrow(()->new IllegalArgumentException("User(reciever) not valid from frontend"));
//
//        Set<String> sessions = userSessionMap.get(user.getEmail());
//
//        if (sessions == null) return false;
//        log.info("Checking is user subscrobed to websocket {} useremail {}", userId,user.getEmail());
//
//        for (String sessionId : sessions) {
//            Set<String> chats = sessionChatMap.get(sessionId);
//            log.info("sessions ids {}",sessionId);
//            if (chats != null && chats.contains(chatId)) {
//                return true;
//            }
//        }
//        return false;
//    }
}
