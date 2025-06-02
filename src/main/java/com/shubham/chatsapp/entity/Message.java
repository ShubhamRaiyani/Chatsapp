package com.shubham.chatsapp.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Entity
@Data
public class Message {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne
    private Chat chat; // For personal chats

    @ManyToOne
    private Group group; // For group chats

    @ManyToOne
    private User sender;

    @ManyToOne
    private User receiver; // For personal messages only

    private String content;
    private Instant createdAt;
    private String messageType; // TEXT, IMAGE, VIDEO, etc.
//    private String mediaUrl;
//    private Boolean isEdited;


}
