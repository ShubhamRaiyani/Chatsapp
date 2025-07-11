package com.shubham.chatsapp.controller;

import com.shubham.chatsapp.dto.MessageDTO;
import com.shubham.chatsapp.dto.MessageRequest;
import com.shubham.chatsapp.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/messages")
public class MessageController {

//    GET /api/messages/chat/{chatId}?page=0&size=20
//            → Returns page of personal chat messages.


//
//    GET /api/messages/group/{groupId}?page=1&size=10
//            → Returns page 1 of group chat messages.


    private final MessageService messageService;

    public MessageController(MessageService messageService) {
        this.messageService = messageService;
    }

    @GetMapping("/chat/{chatId}")
    public Page<MessageDTO> getChatMessages(
            @PathVariable UUID chatId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return messageService.getMessagesForChat(chatId, page, size);
    }

    @GetMapping("/group/{groupId}")
    public Page<MessageDTO> getGroupMessages(
            @PathVariable UUID groupId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return messageService.getMessagesForGroup(groupId, page, size);
    }

}