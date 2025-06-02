package com.shubham.chatsapp.dto;

import lombok.Data;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
@Data
public class ChatDetailsDTO {
    private UUID chatId;
    private String displayName;  // Other user name for personal chat, or group name
    private boolean isGroup;
    private Instant lastActivity;
    private List<String> participantEmails;  // Optional, emails of chat/group members
}
