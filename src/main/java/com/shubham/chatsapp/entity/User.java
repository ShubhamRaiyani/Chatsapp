package com.shubham.chatsapp.entity;

import jakarta.persistence.*;
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
    @Column(unique = true)
    private String email;
    private boolean enabled ; // for email verifivation
    private String bio;
    private String status;
    private Instant createdAt;
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private VerificationToken verificationToken;



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




















