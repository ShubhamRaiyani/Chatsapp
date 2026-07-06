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
import org.springframework.web.bind.annotation.*;

import java.util.Map;

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
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not foundss"));
        user.setStatus("online");
        return ResponseEntity.ok(new UserProfile(user));
    }

    @GetMapping("/online")
    public ResponseEntity<Set<String>> getOnlineUsers() {
        return ResponseEntity.ok(sessionTracker.getAllOnlineEmails());
    }

    @GetMapping("/check-username")
    public ResponseEntity<java.util.Map<String, Boolean>> checkUsername(
            @RequestParam String username,
            Authentication authentication) {
        String currentUserEmail = authentication.getName();
        java.util.Optional<User> existing = userRepository.findFirstByUsername(username.trim());
        boolean available = existing.isEmpty() || existing.get().getEmail().equals(currentUserEmail);
        return ResponseEntity.ok(java.util.Map.of("available", available));
    }

    @PutMapping("/username")
    public ResponseEntity<?> updateUsername(@RequestBody Map<String, String> body, Authentication authentication) {
        String newUsername = body.get("username");
        if (newUsername == null || newUsername.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Username cannot be empty");
        }
        String trimmed = newUsername.trim();
        if (trimmed.length() < 3) {
            return ResponseEntity.badRequest().body("Username must be at least 3 characters");
        }
        String email = authentication.getName();
        // Uniqueness check — allow keeping the same username
        java.util.Optional<User> existing = userRepository.findFirstByUsername(trimmed);
        if (existing.isPresent() && !existing.get().getEmail().equals(email)) {
            return ResponseEntity.badRequest().body("Username is already taken");
        }
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        user.setUsername(trimmed);
        userRepository.save(user);
        return ResponseEntity.ok(new UserProfile(user));
    }

    @GetMapping("/search")
    public ResponseEntity<List<UserDTO>> searchUsers(@RequestParam String emailFragment) {
        List<User> users = userRepository.findByEmailUsernameContainingAndEnabledTrue(emailFragment);
        return ResponseEntity.ok(users.stream().map(UserDTO::new).toList());
    }

}
