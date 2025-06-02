package com.shubham.chatsapp.dto;

import lombok.Data;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Data
public class ChatDTO {
    private UUID id;              // Chat or Group ID
    private String displayName;   // Other user name or group name
    private boolean isGroup;
    private Instant lastActivity;
}
