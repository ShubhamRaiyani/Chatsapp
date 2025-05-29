package com.shubham.chatsapp.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Entity
@Data
public class Chat {
    @Id
    @GeneratedValue
    private UUID id;

    @OneToOne(cascade = CascadeType.ALL)
    private Message message;

    private Instant timestamp;

    // You could add delivery status here in future
}
