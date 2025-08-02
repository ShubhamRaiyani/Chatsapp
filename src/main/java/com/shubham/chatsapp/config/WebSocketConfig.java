package com.shubham.chatsapp.config;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.core.Authentication;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final WebSocketAuthInterceptor webSocketAuthInterceptor;
    @Value("${frontend.url}")
    private String frontendURL;

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // ✅ DO NOT call .setTaskScheduler() to avoid circular reference
        config.enableSimpleBroker("/topic", "/queue");
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setAllowedOrigins(frontendURL)
                .addInterceptors(webSocketAuthInterceptor)
                .withSockJS();

    }
    
    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(new ChannelInterceptor() {
            @Override
            public Message<?> preSend(Message<?> message, MessageChannel channel) {
                StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

                if (accessor != null) {
                    if (StompCommand.CONNECT.equals(accessor.getCommand())) {
                        var user = accessor.getSessionAttributes().get("user");
                        if (user instanceof Authentication auth && auth.isAuthenticated()) {
                            accessor.setUser(auth);
                            System.out.println("✅ STOMP user set: " + auth.getName());
                        } else {
                            System.out.println("❌ STOMP CONNECT rejected: No authentication");
                            return null;
                        }
                    } else if (accessor.getUser() == null) {
                        var user = accessor.getSessionAttributes().get("user");
                        if (user instanceof Authentication auth && auth.isAuthenticated()) {
                            accessor.setUser(auth);
                        } else {
                            System.out.println("❌ STOMP frame rejected: No user authentication");
                            return null;
                        }
                    }
                }

                return message;
            }
        });
    }
}
