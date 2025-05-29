package com.shubham.chatsapp.controller;

import com.shubham.chatsapp.dto.ChatDTO;
import com.shubham.chatsapp.dto.CreateChatRequest;
import com.shubham.chatsapp.dto.GroupCreateRequest;
import com.shubham.chatsapp.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/chats")
public class ChatController {
    private final ChatService chatService;

    @PostMapping("/personal")
    public ResponseEntity<ChatDTO> createPersonalChat(@RequestBody CreateChatRequest request, Authentication authentication) {
        String currentUserEmail = authentication.getName();
        return ResponseEntity.ok(chatService.createPersonalChat(currentUserEmail, request.getReceiverEmail()));
    }

    @PostMapping("/group")
    public ResponseEntity<ChatDTO> createGroupChat(@RequestBody GroupCreateRequest request, Authentication authentication) {
        String creatorEmail = authentication.getName();
        return ResponseEntity.ok(chatService.createGroupChat(creatorEmail, request));
    }

    @GetMapping
    public ResponseEntity<List<ChatDTO>> getAllChats(Authentication authentication) {
        String currentUserEmail = authentication.getName();
        return ResponseEntity.ok(chatService.getAllChats(currentUserEmail));
    }
}
