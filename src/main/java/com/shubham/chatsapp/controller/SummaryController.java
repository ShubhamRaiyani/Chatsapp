package com.shubham.chatsapp.controller;

import com.cohere.api.types.ChatResponse;
import com.cohere.api.types.TextContent;
import com.shubham.chatsapp.entity.ChatSummaries;
import com.shubham.chatsapp.service.ChatSummaryService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<String> summarizeChat(@PathVariable UUID chatId) {
        ChatResponse response = chatSummaryService.generateChatSummary(chatId);
        System.out.println("ChatResponse: " + response);

        if (response == null || response.getMessage() == null ||
                response.getMessage().getContent() == null || response.getMessage().getContent().isEmpty()) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to generate summary.");
        }

        Optional<TextContent> optionalContent = response.getMessage().getContent().get().get(0).getText();
        String summary = optionalContent.map(TextContent::getText).orElse("Summary not available");

        return ResponseEntity.ok(summary);
    }
}
