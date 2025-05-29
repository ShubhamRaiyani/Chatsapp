package com.shubham.chatsapp.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class MessageDTO {
    private String content;
    private String senderUsername;
    private UUID chatId;
    private LocalDateTime sentAt;
    private String messageType;

}
