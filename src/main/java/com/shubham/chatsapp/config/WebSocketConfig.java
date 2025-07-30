package com.shubham.chatsapp.config;

import com.shubham.chatsapp.config.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import java.util.List;

@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final JwtService jwtService;

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic");  // Clients will subscribe to /topic/*
        config.setApplicationDestinationPrefixes("/app"); // Messages sent to /app/*
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws") // WebSocket endpoint
                .setAllowedOrigins("http://localhost:5173") // Replace with frontend origin
                .withSockJS(); // Enable SockJS fallback
    }

//    @Override
//    public void configureClientInboundChannel(ChannelRegistration registration) {
//        registration.interceptors(new ChannelInterceptor() {
//            @Override
//            public Message<?> preSend(Message<?> message, MessageChannel channel) {
//                StompHeaderAccessor accessor =
//                        MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
//
//                if (accessor != null && accessor.getUser() == null) {
//                    List<String> authHeader = accessor.getNativeHeader("Authorization");
//
//                    if (authHeader != null && !authHeader.isEmpty()) {
//                        try {
//                            String token = authHeader.get(0).replace("Bearer ", "");
//                            Authentication authentication = jwtService.getAuthentication(token);
//                            accessor.setUser(authentication); // ✅ Inject user into WebSocket session
//                        } catch (Exception e) {
//                            System.out.println("JWT error: " + e.getMessage());
//                        }
//                    }
//                }
//                return message;
//            }
//        });
//    }
}
