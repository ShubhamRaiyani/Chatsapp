package com.shubham.chatsapp.dto;

import lombok.Data;
import org.springframework.cglib.core.Local;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class GroupDTO {
    private Long id;
    private String groupName;
    private List<UserDTO> members;
    private Long createdBy;
    private LocalDateTime createdAt;

}
