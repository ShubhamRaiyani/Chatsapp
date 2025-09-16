package com.shubham.chatsapp.controller;

import com.shubham.chatsapp.dto.ChatDTO;
import com.shubham.chatsapp.dto.ChatDetailsDTO;
import com.shubham.chatsapp.dto.CreateChatRequest;
import com.shubham.chatsapp.dto.GroupCreateRequest;
import com.shubham.chatsapp.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

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
        String currentUserEmail = authentication.getName();
        return ResponseEntity.ok(chatService.createGroupChat(currentUserEmail, request));
    }

    @GetMapping   // dashboard chats
    public ResponseEntity<List<ChatDTO>> getAllChats(Authentication authentication) {
        String currentUserEmail = authentication.getName();
        return ResponseEntity.ok(chatService.getAllChats(currentUserEmail));
    }


    @GetMapping("/chat/{chatId}/details")
    public ResponseEntity<ChatDetailsDTO> getChatDetails(
            @PathVariable UUID chatId,
            @RequestParam String currentUserEmail) {
        System.out.println("inside the controller ");
        ChatDetailsDTO details = chatService.getChatDetails(chatId, currentUserEmail);
        return ResponseEntity.ok(details);
    }

    @DeleteMapping("/group/{groupId}/leave")
    public ResponseEntity<?> leaveGroup(
            @PathVariable UUID groupId,
            Authentication authentication) {
        String currentUserEmail = authentication.getName();
        try {
            chatService.leaveGroup(groupId, currentUserEmail);
            return ResponseEntity.ok().body("Successfully left the group");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }



}
