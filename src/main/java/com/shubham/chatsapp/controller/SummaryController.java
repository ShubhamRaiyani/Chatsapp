package com.shubham.chatsapp.controller;

import com.cohere.api.types.ChatResponse;
import com.cohere.api.types.TextContent;
import com.shubham.chatsapp.entity.ChatSummaries;
import com.shubham.chatsapp.service.ChatSummaryService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/summary")
public class SummaryController {

    private final ChatSummaryService chatSummaryService;

    public SummaryController(ChatSummaryService chatSummaryService) {
        this.chatSummaryService = chatSummaryService;
    }

    @GetMapping("/{chatId}")
    public ResponseEntity<String> summarizeChat(@PathVariable UUID chatId, Authentication authentication) {
        String authenticatedEmail = authentication.getName();

        try {
            String summary = chatSummaryService.generateChatSummaryText(chatId, authenticatedEmail);
            if (summary == null || summary.trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("No messages found in the last 2 days to summarize.");
            }
            return ResponseEntity.ok(summary);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to generate summary: " + e.getMessage());
        }
    }


}
