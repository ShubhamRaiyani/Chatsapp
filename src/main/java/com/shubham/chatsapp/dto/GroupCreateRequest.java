package com.shubham.chatsapp.dto;

import lombok.Data;

import java.util.List;

@Data
public class GroupCreateRequest {
    private String name;
    private List<String> memberEmails;
}
