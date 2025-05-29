package com.shubham.chatsapp.dto;

import lombok.Data;

@Data
public class MessageRequest {
    private Long chatId;
    private String content;
    private String messageType;
}
