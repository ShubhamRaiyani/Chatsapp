package com.shubham.chatsapp.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
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
    @OneToMany(mappedBy = "group", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Message> messages = new ArrayList<>();

}
