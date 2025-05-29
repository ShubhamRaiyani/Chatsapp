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
    private User sender;

    @ManyToOne
    private User receiver;

    @ManyToOne
    private Group group;

    private String content;
    //    private String messageType; // TEXT, IMAGE, VIDEO, etc.
//    private String mediaUrl;
//    private Boolean isEdited;
    private Instant createdAt;


}
