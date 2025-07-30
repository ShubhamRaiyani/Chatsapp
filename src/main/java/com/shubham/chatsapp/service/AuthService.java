package com.shubham.chatsapp.service;

import com.shubham.chatsapp.config.CustomUserDetailsService;
import com.shubham.chatsapp.config.JwtService;
import com.shubham.chatsapp.dto.AuthResponse;
import com.shubham.chatsapp.dto.LoginRequest;
import com.shubham.chatsapp.dto.RegisterRequest;
import com.shubham.chatsapp.entity.VerificationToken;
import com.shubham.chatsapp.entity.User;
import com.shubham.chatsapp.repository.UserRepository;
import com.shubham.chatsapp.repository.VerificationTokenRepository;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
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
@Slf4j
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final CustomUserDetailsService userDetailsService;
    private final UserRepository userRepository;
    private final VerificationTokenRepository tokenRepository;
    private final emailService emailService;
    private final PasswordEncoder passwordEncoder;

    public AuthService(AuthenticationManager authenticationManager, JwtService jwtService, CustomUserDetailsService userDetailsService, UserRepository userRepository, VerificationTokenRepository tokenRepository, com.shubham.chatsapp.service.emailService emailService, PasswordEncoder passwordEncoder) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
        this.emailService = emailService;
        this.passwordEncoder = passwordEncoder;
    }


    public boolean registerUser(RegisterRequest request) {
        Optional<User> existingUser = userRepository.findByEmail(request.getEmail());

        // Case 1: No user exists — proceed with registration
        if (existingUser.isEmpty()) {
            return registerNewUser(request);
        }

        // Case 2: User exists but not verified — resend verification
        if (!existingUser.get().isEnabled()) {
            return registerNewUser(request); // Optionally, re-send verification token instead of recreating user
        }

        // Case 3: User already verified
        return false;
    }
    private boolean registerNewUser(RegisterRequest request) {
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
        VerificationToken verificationToken =
                new VerificationToken(token, user, Instant.now().plus(1, ChronoUnit.DAYS));

        VerificationToken savedToken = tokenRepository.save(verificationToken);

        log.info("Saved Token: {}", savedToken.getToken());

        String verificationUrl = "http://localhost:5173/verify?token=" + token;
        emailService.sendVerificationEmail(user.getEmail(), user.getUsername(), verificationUrl);

        return true;
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
        userRepository.save(user);

//        tokenRepository.delete(verificationToken);
    }

    public ResponseEntity<AuthResponse> loginVerify(LoginRequest request, HttpServletResponse response) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        UserDetails user = userDetailsService.loadUserByUsername(request.getEmail());
        String jwtToken = jwtService.generateToken(user);

        // Create secure HttpOnly cookie for the JWT
        ResponseCookie jwtCookie = ResponseCookie.from("AUTH-TOKEN", jwtToken)
                .httpOnly(true)
                .secure(true)
                .sameSite("Strict")       // Or "Lax" depending on your needs
                .path("/")
                .maxAge(7 * 24 * 60 * 60)
                .build();

        // Add Set-Cookie to response header
        response.addHeader(HttpHeaders.SET_COOKIE, jwtCookie.toString());

        // Optionally, you can omit returning the JWT in the body for extra security
        return ResponseEntity.ok(new AuthResponse(null, request.getEmail()));
    }
}
