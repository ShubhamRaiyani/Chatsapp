package com.shubham.chatsapp.controller;

import com.shubham.chatsapp.config.JwtService;
import com.shubham.chatsapp.dto.UserDTO;
import com.shubham.chatsapp.dto.UserProfile;
import com.shubham.chatsapp.entity.User;
import com.shubham.chatsapp.repository.UserRepository;
import com.shubham.chatsapp.service.WebSocketSessionTracker;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserRepository userRepository;
    private final WebSocketSessionTracker sessionTracker;

    public UserController(UserRepository userRepository, JwtService jwtService, WebSocketSessionTracker sessionTracker) {
        this.userRepository = userRepository;
        this.sessionTracker = sessionTracker;
    }

    @GetMapping("/me")
    public ResponseEntity<UserProfile> getMyProfile(Authentication authentication) {
        String email = authentication.getName();
        System.out.println(email);
        System.out.println(authentication + "hello");
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not foundss"));
        user.setStatus("online");
        return ResponseEntity.ok(new UserProfile(user));
    }

    @GetMapping("/online")
    public ResponseEntity<Set<String>> getOnlineUsers() {
        return ResponseEntity.ok(sessionTracker.getAllOnlineEmails());
    }

    @GetMapping("/search")
    public ResponseEntity<List<UserDTO>> searchUsers(@RequestParam String emailFragment) {
        List<User> users = userRepository.findByEmailUsernameContainingAndEnabledTrue(emailFragment);
        return ResponseEntity.ok(users.stream().map(UserDTO::new).toList());
    }

}
