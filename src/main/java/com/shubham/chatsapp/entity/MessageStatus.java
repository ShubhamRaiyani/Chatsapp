package com.shubham.chatsapp.entity;

import com.shubham.chatsapp.enums.StatusType;
import jakarta.persistence.*;
import lombok.Data;
import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

@Entity
@Data
@IdClass(MessageStatusId.class) // Composite key: message + user
public class MessageStatus {

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "message_id", nullable = false)
    private Message message;

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    private StatusType status; // SENT, DELIVERED, READ

    private Instant updatedAt;
}
