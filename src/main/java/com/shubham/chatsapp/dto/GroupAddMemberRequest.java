package com.shubham.chatsapp.dto;

import lombok.Data;

import java.util.List;

@Data
public class GroupAddMemberRequest {
    private Long groupId;
    private List<Long> newMemberIds;
}
