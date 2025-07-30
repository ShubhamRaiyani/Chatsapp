package com.shubham.chatsapp.dto;

import lombok.Data;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class MessageDTO {
    private UUID messageId;
    private String content;
//    private String senderEmail;
    private String receiverEmail;
    private UUID chatId;
    private UUID groupId;
    private LocalDateTime sentAt;
    private String messageType;

}
