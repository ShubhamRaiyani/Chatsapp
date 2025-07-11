package com.shubham.chatsapp.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.function.Function;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
public class JwtService {
    @Value("${app.jwt.secret}")
    private String jwtSecret;
    @Value("${app.jwt.expirationMs}")
    private long jwtExpirationMs;

    public JwtService() {
    }

    private Key getSigningKey() {
        byte[] keyBytes = this.jwtSecret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateToken(UserDetails userDetails) {
        return ((JwtBuilder) ((JwtBuilder) ((JwtBuilder) Jwts.builder().subject(userDetails.getUsername())).issuedAt(new Date())).expiration(new Date(System.currentTimeMillis() + this.jwtExpirationMs))).signWith(this.getSigningKey(), SignatureAlgorithm.HS256).compact();
    }

    public String extractEmail(String token) {
        return (String)this.extractClaim(token, Claims::getSubject);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        Claims claims = this.extractAllClaims(token);
        return (T)claimsResolver.apply(claims);
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        String email = this.extractEmail(token);
        return email.equals(userDetails.getUsername()) && !this.isTokenExpired(token);
    }

    private boolean isTokenExpired(String token) {
        return this.extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return (Date)this.extractClaim(token, Claims::getExpiration);
    }

    private Claims extractAllClaims(String token) {
        return (Claims)Jwts.parser().setSigningKey(this.getSigningKey()).build().parseClaimsJws(token).getBody();
    }
}
