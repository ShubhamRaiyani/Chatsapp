package com.shubham.chatsapp.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Data
public class ChatDTO {
    private UUID id;              // Chat or Group ID
    private String displayName;
    @JsonProperty("isGroup")
    private boolean isGroup;
    private Instant lastActivity;
    private String lastMessage;
    private Long unreadCount; // new add (left handling it in backend)
    private String receiverEmail;
    private List<String> participantEmails;  // , emails of chat/group members


}
