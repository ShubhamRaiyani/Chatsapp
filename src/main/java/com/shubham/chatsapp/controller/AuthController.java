package com.shubham.chatsapp.controller;


import com.shubham.chatsapp.dto.AuthResponse;
import com.shubham.chatsapp.dto.LoginRequest;
import com.shubham.chatsapp.dto.RegisterRequest;
import com.shubham.chatsapp.service.AuthService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Generated;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
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

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody RegisterRequest request) {
        try {
            boolean registered = this.authService.registerUser(request);

            if (!registered) {
                return ResponseEntity
                        .status(HttpStatus.BAD_REQUEST)
                        .body(new AuthResponse("Email already verified or username taken", null));
            }

            return ResponseEntity
                    .ok(new AuthResponse("Check your email for verification link.", null));

        } catch (DataIntegrityViolationException e) {
            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body(new AuthResponse("Email or username already exists", null));
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new AuthResponse("Registration failed due to server error", null));
        }
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
        try {
            return authService.loginVerify(req, response);
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new AuthResponse("Invalid credentials", null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new AuthResponse("Something went wrong", null));
        }
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
