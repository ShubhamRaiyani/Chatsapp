package com.shubham.chatsapp.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "app_user")
public class User {
    @Id
    @GeneratedValue
    private UUID id;
    private String username;
    private String password;
    private String email;
    private boolean enabled ; // for email verifivation
    private String bio;
    private String status;
    private Instant createdAt;


//    public String getEmail() {
//        return email;
//    }
//    public String getPassword() {
//        return password;
//    }
//
//    public UUID getId() {
//        return id;
//    }
//
//    public void setId(UUID id) {
//        this.id = id;
//    }
//
//    public String getUsername() {
//        return username;
//    }
//
//    public void setUsername(String username) {
//        this.username = username;
//    }
//
//
//    public void setPassword(String password) {
//        this.password = password;
//    }
//
//    public void setEmail(String email) {
//        this.email = email;
//    }
//
//    public String getStatus() {
//        return status;
//    }
//
//    public void setStatus(String status) {
//        this.status = status;
//    }
//
//    public boolean isEnabled() {
//        return enabled;
//    }
//
//    public void setEnabled(boolean enabled) {
//        this.enabled = enabled;
//    }
//
//    public Instant getCreatedAt() {
//        return createdAt;
//    }
//
//    public void setCreatedAt(Instant createdAt) {
//        this.createdAt = createdAt;
//    }
}


//        +---------+       +-----------+       +------+
//        |  User   |<----- |  Message  | ----> | Chat |
//        +---------+       +-----------+       +------+
//        ^                 |   ^               |
//        |                 |   |               |
//        |       +---------+   +--------+      |
//        |       |                      |      |
//        |    receiver              sender     |
//        |                                     |
//        +------------------+-----------------+
//                           |
//                       +--------+
//                       | Group  |
//                        +--------+




















