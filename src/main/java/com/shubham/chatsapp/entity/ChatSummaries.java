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
public class ChatSummaries {
    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne
    private Group group;

    @ManyToOne
    private User receiver;

    private String summaryText;
    private Instant generatedAt;
}
