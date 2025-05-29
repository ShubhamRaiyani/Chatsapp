package com.shubham.chatsapp.service;

import com.nimbusds.openid.connect.sdk.AuthenticationResponse;
import com.shubham.chatsapp.config.CustomUserDetailsService;
import com.shubham.chatsapp.config.JwtService;
import com.shubham.chatsapp.dto.AuthResponse;
import com.shubham.chatsapp.dto.LoginRequest;
import com.shubham.chatsapp.dto.RegisterRequest;
import com.shubham.chatsapp.dto.VerificationToken;
import com.shubham.chatsapp.entity.User;
import com.shubham.chatsapp.repository.UserRepository;
import com.shubham.chatsapp.repository.VerificationTokenRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;

@Service
public class AuthService {
    @Autowired
    AuthenticationManager authenticationManager;
    @Autowired
    private  JwtService jwtService;
    @Autowired
    CustomUserDetailsService userDetailsService;

    private final UserRepository userRepository;
    private final VerificationTokenRepository tokenRepository;
    private final emailService emailService;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserRepository userRepository, VerificationTokenRepository tokenRepository, com.shubham.chatsapp.service.emailService emailService, PasswordEncoder passwordEncoder) {

        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
        this.emailService = emailService;
        this.passwordEncoder = passwordEncoder;
    }


    public void registerUser(RegisterRequest request) {
        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .enabled(false)
                .createdAt(Instant.now())
                .status("pending")
                .build();
        userRepository.save(user);

        String token = UUID.randomUUID().toString();
        VerificationToken verificationToken = new VerificationToken(token, user, Instant.now().plus(1, ChronoUnit.DAYS));
        VerificationToken savedToken = tokenRepository.save(verificationToken);
        System.out.println("Saved Token ID: " + savedToken.getId());
        System.out.println("Saved Token String: " + savedToken.getToken());
        System.out.println("Saved Token User: " + savedToken.getUser().getEmail());
        Optional<VerificationToken> check = tokenRepository.findByToken(token);
        if (check.isPresent()) {
            System.out.println("Token successfully saved.");
        } else {
            System.out.println("Token not saved!");
        }

        String verificationUrl = "http://localhost:5173/verify?token=" + token; // React handles token on frontend
        emailService.sendEmail(user.getEmail(), "Verify Your Email", "Click to verify: " + verificationUrl);
    }
    @Transactional
    public void verifyToken(String token) {
        VerificationToken verificationToken = tokenRepository.findByToken(token)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid verification token"));

        if (verificationToken.getExpiryDate().isBefore(Instant.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Token expired");
        }

        User user = verificationToken.getUser();
        user.setEnabled(true);
        user.setStatus("active");
        userRepository.save(user);

//        tokenRepository.delete(verificationToken); // Optional
    }

    public ResponseEntity<AuthResponse> verify(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        // Load user details
        UserDetails user = userDetailsService.loadUserByUsername(request.getEmail());

        // Generate JWT
        String jwtToken = jwtService.generateToken(user);

        // Return token in response
        return ResponseEntity.ok(new AuthResponse(jwtToken, request.getEmail()) {
        });
    }
}
