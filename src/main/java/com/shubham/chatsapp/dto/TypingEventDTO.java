package com.shubham.chatsapp.dto;

import lombok.Data;

@Data
public class TypingEventDTO {
    private String type;
    private String chatId;
    private String groupId;
    private String userId;    // sender email
    private String userName;
    private Boolean isTyping;
}
