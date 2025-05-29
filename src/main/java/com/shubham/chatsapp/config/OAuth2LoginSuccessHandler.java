package com.shubham.chatsapp.config;

import com.shubham.chatsapp.entity.User;
import com.shubham.chatsapp.repository.UserRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Instant;
import lombok.Generated;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

@Component
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {
    private final JwtService jwtService;
    private final UserRepository userRepository;

    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        DefaultOAuth2User oauthUser = (DefaultOAuth2User)authentication.getPrincipal();
        String email = (String)oauthUser.getAttribute("email");
        String rawname = (String)oauthUser.getAttribute("name");
        String name = rawname != null ? rawname : email;
        if (rawname == null) {
            ;
        }

        if (email == null) {
            throw new RuntimeException("Email not found from OAuth provider");
        } else {
            User user = (User)this.userRepository.findByEmail(email).orElseGet(() -> {
                User newUser = User.builder().email(email).username(name).password("").enabled(true).createdAt(Instant.now()).status("active").build();
                return (User)this.userRepository.save(newUser);
            });
            UserDetails userDetails = org.springframework.security.core.userdetails.User.withUsername(user.getEmail()).password("").authorities(new String[]{"USER"}).build();
            String token = this.jwtService.generateToken(userDetails);
            response.sendRedirect("http://localhost:5173/oauth2/redirect?token=" + token);
        }
    }

    @Generated
    public OAuth2LoginSuccessHandler(final JwtService jwtService, final UserRepository userRepository) {
        this.jwtService = jwtService;
        this.userRepository = userRepository;
    }
}
