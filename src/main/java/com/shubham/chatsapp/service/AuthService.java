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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
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
    @Value("${frontend.url}")
    private String frontendURL;


    @Transactional
    public boolean registerUser(RegisterRequest request) {
        Optional<User> existingUserOpt = userRepository.findByEmail(request.getEmail());

        if (existingUserOpt.isEmpty()) {
            return registerNewUser(request);
        }

        User existingUser = existingUserOpt.get();
        if (!existingUser.isEnabled()) {
            // Update unverified user
            existingUser.setUsername(request.getUsername());
            existingUser.setPassword(passwordEncoder.encode(request.getPassword()));
            existingUser.setCreatedAt(Instant.now());
            existingUser.setStatus("pending");
            userRepository.save(existingUser);
            emailTokenGenerateAndSend(existingUser);
            return true;
        }

        // User exists and is verified
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

        emailTokenGenerateAndSend(user);
        return true;
    }

    @Transactional
    public void emailTokenGenerateAndSend(User user) {
        Optional<VerificationToken> existingTokenOpt = tokenRepository.findByUser(user);

        String newTokenStr = UUID.randomUUID().toString();
        Instant expiry = Instant.now().plus(1, ChronoUnit.DAYS);

        VerificationToken token;
        if (existingTokenOpt.isPresent()) {
            token = existingTokenOpt.get();
            token.setToken(newTokenStr);
            token.setExpiryDate(expiry);
        } else {
            token = new VerificationToken(newTokenStr, user, expiry);
        }

        tokenRepository.save(token);

        String verificationUrl = frontendURL + "/verify?token=" + newTokenStr;
        emailService.sendVerificationEmail(user.getEmail(), user.getUsername(), verificationUrl);
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
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );

            UserDetails user = (UserDetails) authentication.getPrincipal();
            log.info("User found: {}", user.getUsername());

            String jwtToken = jwtService.generateToken(user);

            ResponseCookie jwtCookie = ResponseCookie.from("AUTH-TOKEN", jwtToken)
                    .httpOnly(true)
                    .secure(true) // false in local dev if needed
                    .sameSite("None")
                    .path("/")
                    .maxAge(7 * 24 * 60 * 60)
                    .build();

            response.addHeader(HttpHeaders.SET_COOKIE, jwtCookie.toString());

            return ResponseEntity.ok(new AuthResponse(null, user.getUsername()));

        } catch (BadCredentialsException e) {
            throw e; // Let the controller handle this
        } catch (Exception e) {
            log.error("Login error: ", e);
            throw e; // Let the controller catch and respond with 500
        }
    }


}
