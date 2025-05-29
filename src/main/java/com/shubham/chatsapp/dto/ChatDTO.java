package com.shubham.chatsapp.dto;

import lombok.Data;

import java.util.List;

@Data
public class ChatDTO {
    private String name;
    private boolean isGroup;
    private String lastMessage;
}
