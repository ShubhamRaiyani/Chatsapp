package com.shubham.chatsapp.controller;


import com.shubham.chatsapp.dto.AuthResponse;
import com.shubham.chatsapp.dto.LoginRequest;
import com.shubham.chatsapp.dto.RegisterRequest;
import com.shubham.chatsapp.service.AuthService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Generated;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server   .ResponseStatusException;

import java.util.Map;

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
        if (this.authService.registerUser(request) == false) {
            return ResponseEntity.badRequest().body("Username already taken");
        }
        this.authService.registerUser(request);
        return ResponseEntity.ok("Check your email for verification link.");
//        if (this.authService.registerUser(request) == false){
//            return (ResponseEntity<String>) ResponseEntity.badRequest();
//        }else {
//
//        }

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

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest req, HttpServletResponse response) {
        return this.authService.loginVerify(req, response);
    }

    @PostMapping("/oauth2/callback")
    public ResponseEntity<?> oauth2Callback(@RequestBody Map<String, String> body, HttpServletResponse response) {
        String token = body.get("token");
        if (token == null) {
            return ResponseEntity.badRequest().build();
        }
        // Set cookie with HttpOnly flag and secure flags as appropriate
        ResponseCookie cookie = ResponseCookie.from("AUTH-TOKEN", token)
                .httpOnly(true)
                .secure(true)
                .path("/")
                .sameSite("Strict")
                .maxAge(60 * 60 * 24 * 7)
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

        return ResponseEntity.ok().build();
    }


}
