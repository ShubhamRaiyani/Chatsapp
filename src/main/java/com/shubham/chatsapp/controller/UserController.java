package com.shubham.chatsapp.controller;

import com.shubham.chatsapp.config.JwtService;
import com.shubham.chatsapp.dto.UserDTO;
import com.shubham.chatsapp.dto.UserProfile;
import com.shubham.chatsapp.entity.User;
import com.shubham.chatsapp.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserRepository userRepository;

    public UserController(UserRepository userRepository, JwtService jwtService) {
        this.userRepository = userRepository;
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

    @GetMapping("/search")
    public ResponseEntity<List<UserDTO>> searchUsers(@RequestParam String emailFragment) {
        List<User> users = userRepository.findByEmailContainingIgnoreCaseAndEnabledTrue(emailFragment);
        return ResponseEntity.ok(users.stream().map(UserDTO::new).toList());
    }

}
