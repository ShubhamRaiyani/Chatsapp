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
public class Group {

    @Id
    @GeneratedValue
    private UUID id;
    private String name;
    @ManyToOne
    private User createdBy;
    private Instant createdAt;


}
