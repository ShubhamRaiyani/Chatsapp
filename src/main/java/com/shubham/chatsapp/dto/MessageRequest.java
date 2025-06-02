package com.shubham.chatsapp.dto;

import lombok.Data;

import java.util.UUID;

@Data
public class MessageRequest {
    private UUID chatId;
    private UUID groupId;
    private String content;
    private String messageType;
}
