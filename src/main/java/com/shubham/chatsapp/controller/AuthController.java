package com.shubham.chatsapp.controller;


import com.shubham.chatsapp.dto.AuthResponse;
import com.shubham.chatsapp.dto.LoginRequest;
import com.shubham.chatsapp.dto.RegisterRequest;
import com.shubham.chatsapp.service.AuthService;
import lombok.Generated;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping({"/api/auth"})
public class AuthController {
    private final AuthService authService;

    @Generated
    public AuthController(final AuthService authService) {
        this.authService = authService;
    }
    @PostMapping({"/register"})
    public ResponseEntity<String> register(@RequestBody RegisterRequest request) {
        this.authService.registerUser(request);
        return ResponseEntity.ok("Check your email for verification link.");
    }

    @GetMapping({"/verify"})
    public ResponseEntity<String> verifyEmail(@RequestParam("token") String token) {
        try {
            this.authService.verifyToken(token);
            return ResponseEntity.ok("Email verified successfully!");
        } catch (ResponseStatusException ex) {
            return ResponseEntity.status(ex.getStatusCode()).body(ex.getReason());
        }
    }

    @PostMapping({"/login"})
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest req) {
        return this.authService.verify(req);
    }

}
