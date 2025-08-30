package com.shubham.chatsapp.dto;

import lombok.Data;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
public class MessageDTO {
    private UUID messageId;
    private String content;
    private String senderEmail;
    private UUID chatId;
    private UUID groupId;

    private String receiverEmail;
//    private List<String> participantEmails;  // , emails of chat/group members

    private LocalDateTime sentAt;
    private String messageType;

}
