package com.shubham.chatsapp.dto;

import com.shubham.chatsapp.entity.User;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserProfile {
    private String username;
    private String email;
    private String bio;
    private String status;
    private Instant createdAt;

    public UserProfile(User user) {
        this.username = user.getUsername();
        this.email = user.getEmail();
        this.bio = user.getBio();
        this.status = user.getStatus();
        this.createdAt = user.getCreatedAt();
    }
}
