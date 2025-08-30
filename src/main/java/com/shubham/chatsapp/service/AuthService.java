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
    @Value("${cookies.secure:false}")
    private Boolean cookies_secure;
    @Value("${cookies.samesite}")
    private String cookies_samesite;

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
        User save = userRepository.save(user);
        System.out.println(save.isEnabled());

//        tokenRepository.delete(verificationToken);
    }

    public ResponseEntity<AuthResponse> loginVerify(LoginRequest request, HttpServletResponse response) {
        try {
            // Step 1: Check if user exists by email
            Optional<User> userOpt = userRepository.findByEmail(request.getEmail());

            if (userOpt.isEmpty()) {
                return ResponseEntity
                        .status(HttpStatus.NOT_FOUND)
                        .body(new AuthResponse("Account with this email does not exist", null));
            }

            User user = userOpt.get();

            // Step 2: Check if email is verified
            if (!user.isEnabled()) {
                return ResponseEntity
                        .status(HttpStatus.FORBIDDEN)
                        .body(new AuthResponse("Please verify your email before logging in", null));
            }

            // Step 3: Authenticate email and password
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );

            UserDetails userDetails = (UserDetails) authentication.getPrincipal();

            // Step 4: Generate JWT token and set as HttpOnly cookie
            String jwtToken = jwtService.generateToken(userDetails);

            ResponseCookie jwtCookie = ResponseCookie.from("AUTH-TOKEN", jwtToken)
                    .httpOnly(true)
                    .secure(cookies_secure) // false for local dev
                    .sameSite(cookies_samesite)
                    .path("/")
                    .maxAge(7 * 24 * 60 * 60)
                    .build();
            System.out.println("Cookies auth servce= "+ jwtCookie.toString());

            response.addHeader(HttpHeaders.SET_COOKIE, jwtCookie.toString());

            return ResponseEntity.ok(new AuthResponse(null, user.getEmail()));

        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new AuthResponse("Invalid password", null));
        } catch (Exception e) {
            log.error("Login error: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new AuthResponse("Login failed due to server error", null));
        }
    }

    public void logoutUser(HttpServletResponse response) {
        // Create an expired cookie to remove it on client side
        ResponseCookie expiredCookie = ResponseCookie.from("AUTH-TOKEN", "")
                .httpOnly(true)
                .secure(cookies_secure) // use false for dev if not HTTPS
                .sameSite(cookies_samesite)
                .path("/")
                .maxAge(0) // expire immediately
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, expiredCookie.toString());
    }


}
