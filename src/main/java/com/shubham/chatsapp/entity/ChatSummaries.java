package com.shubham.chatsapp.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatSummaries {
    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne
    private Chat chat;
    @ManyToOne
    private Group group;


    private String summaryText;
    private Instant generatedAt;
}
