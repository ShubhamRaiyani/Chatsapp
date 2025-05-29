package com.shubham.chatsapp.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class MessageStatusDTO {
    private Long messageId;
    private Long chatId;
    private String status; // SENT, DELIVERED, SEEN
    private LocalDateTime updatedAt;

}
