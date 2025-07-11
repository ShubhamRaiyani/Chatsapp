package com.shubham.chatsapp.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;

import java.time.Instant;
import java.util.UUID;

@Entity
@Data
public class Message {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "chat_id")
    @ToString.Exclude
    private Chat chat;
    // For personal chats

    @ManyToOne
    @JoinColumn(name = "group_id")
    private Group group;

    @ManyToOne
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;

    @ManyToOne
    @JoinColumn(name = "receiver_id")
    private User receiver; // For personal messages only

    private String content;
    private Instant createdAt;
    private String messageType; // TEXT, IMAGE, VIDEO, etc.
//    private String mediaUrl;
//    private Boolean isEdited;


}
