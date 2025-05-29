package com.shubham.chatsapp.dto;

import lombok.Data;

import java.util.List;

@Data
public class GroupCreateRequest {
    private String groupName;
    private List<Long> memberIds;
}
