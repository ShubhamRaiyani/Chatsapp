package com.shubham.chatsapp.config;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

@Component
public class WebSocketAuthInterceptor implements HandshakeInterceptor {

    private final JwtService jwtService;
    private final CustomUserDetailsService userDetailsService;

    public WebSocketAuthInterceptor(JwtService jwtService, CustomUserDetailsService userDetailsService) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
    }

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                   WebSocketHandler wsHandler, Map<String, Object> attributes) {

        if (request instanceof ServletServerHttpRequest servletRequest) {
            HttpServletRequest servlet = servletRequest.getServletRequest();
            String jwt = null;

            // ✅ FIXED: Use same cookie name as JwtAuthenticationFilter
            Cookie[] cookies = servlet.getCookies();
            if (cookies != null) {
                for (Cookie cookie : cookies) {
                    if ("AUTH-TOKEN".equals(cookie.getName())) { // ✅ Changed from "token"
                        jwt = cookie.getValue();
                        break;
                    }
                }
            }

            if (jwt != null) {
                try {
                    String email = jwtService.extractEmail(jwt);
                    if (email != null) {
                        UserDetails userDetails = userDetailsService.loadUserByUsername(email);
                        if (jwtService.isTokenValid(jwt, userDetails)) {
                            // ✅ Create proper Authentication object
                            UsernamePasswordAuthenticationToken auth =
                                    new UsernamePasswordAuthenticationToken(
                                            userDetails, null, userDetails.getAuthorities());

                            attributes.put("user", auth);
                            System.out.println("✅ WebSocket authentication successful for: " + email);
                            return true;
                        }
                    }
                } catch (Exception e) {
                    System.out.println("❌ WebSocket JWT validation failed: " + e.getMessage());
                }
            }
        }

        System.out.println("❌ WebSocket handshake rejected: No valid authentication");
        return false; // ✅ Reject unauthenticated connections
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                               WebSocketHandler wsHandler, Exception exception) {
        if (exception != null) {
            System.out.println("❌ WebSocket handshake error: " + exception.getMessage());
        }
    }
}
