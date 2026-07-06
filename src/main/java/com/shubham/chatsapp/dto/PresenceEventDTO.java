package com.shubham.chatsapp.dto;

import lombok.Data;

@Data
public class PresenceEventDTO {
    private String type;
    private String userId;   // email
    private boolean online;
}
