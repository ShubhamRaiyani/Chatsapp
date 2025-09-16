package com.shubham.chatsapp.service;

import com.cohere.api.Cohere;
import com.cohere.api.core.CohereApiException;
import com.cohere.api.requests.SummarizeRequest;
import com.cohere.api.resources.v2.requests.V2ChatRequest;
import com.cohere.api.types.*;
import com.shubham.chatsapp.entity.Chat;
import com.shubham.chatsapp.entity.Group;
import com.shubham.chatsapp.entity.Message;
import com.shubham.chatsapp.entity.User;
import com.shubham.chatsapp.repository.ChatRepository;
import com.shubham.chatsapp.repository.GroupRepository;
import com.shubham.chatsapp.repository.MessageRepository;
import com.shubham.chatsapp.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;  // ✅ Lombok logger
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class ChatSummaryService {

    @Value("${cohere.api.key}")
    private String apiKey;

    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final ChatRepository chatRepository;
    private final GroupRepository groupRepository;

    public ChatSummaryService(
            MessageRepository messageRepository,
            UserRepository userRepository,
            ChatRepository chatRepository,
            GroupRepository groupRepository
    ) {
        this.messageRepository = messageRepository;
        this.userRepository = userRepository;
        this.chatRepository = chatRepository;
        this.groupRepository = groupRepository;
    }

    public String generateChatSummaryText(UUID chatId, String userEmail) {
        try {
            LocalDateTime twoDaysAgo = LocalDateTime.now().minusDays(2);
            List<Message> messages = messageRepository.findRecentMessages(chatId, twoDaysAgo);
            if (messages.isEmpty()) {
                log.info("No recent messages found for chatId={} within 2 days", chatId);
                return null;
            }

            StringBuilder sb = new StringBuilder("Summarize the following chat give only summary no other details:\n");
            for (Message msg : messages) {
                sb.append(msg.getSender().getUsername())
                        .append(": ")
                        .append(msg.getContent())
                        .append("\n");
            }
            String prompt = sb.toString();

            Cohere cohere = Cohere.builder().token(apiKey).build();
            ChatResponse response = cohere.v2().chat(
                    V2ChatRequest.builder()
                            .model("command-a-03-2025")
                            .messages(List.of(
                                    ChatMessageV2.user(
                                            UserMessage.builder()
                                                    .content(UserMessageContent.of(prompt))
                                                    .build()
                                    )
                            ))
                            .build()
            );

            if (response != null
                    && response.getMessage() != null
                    && response.getMessage().getContent() != null
                    && !response.getMessage().getContent().isEmpty()) {

                String summaryText = response.getMessage()
                        .getContent().get().get(0)
                        .getText().get().getText();

                saveSummaryAsMessage(chatId, userEmail, summaryText);
                return summaryText;
            } else {
                log.warn("Empty response from Cohere for chatId={}", chatId);
                return null;
            }
        } catch (CohereApiException e) {
            log.error("Cohere API error while summarizing chatId={}: {}", chatId, e.getMessage(), e);
        } catch (Exception e) {
            log.error("Unexpected error while generating chat summary for chatId={}", chatId, e);
        }
        return null;
    }

    private void saveSummaryAsMessage(UUID chatId, String userEmail, String summaryContent) {
        if (summaryContent == null || summaryContent.trim().isEmpty()) {
            log.warn("Skipping empty summary for chatId={}", chatId);
            return;
        }

        try {
            User sender = userRepository.findByEmail(userEmail)
                    .orElseThrow(() -> new UsernameNotFoundException("User email invalid: " + userEmail));

            Message summaryMessage = new Message();
            summaryMessage.setSender(sender);
            summaryMessage.setContent(summaryContent);
            summaryMessage.setMessageType("SUMMARY");
            summaryMessage.setCreatedAt(LocalDateTime.now());

            if (isChatId(chatId)) {
                Chat chat = chatRepository.findById(chatId)
                        .orElseThrow(() -> new RuntimeException("Chat not found with ID: " + chatId));
                summaryMessage.setChat(chat);
                summaryMessage.setGroup(null);
            } else if (isGroupId(chatId)) {
                Group group = groupRepository.findById(chatId)
                        .orElseThrow(() -> new RuntimeException("Group not found with ID: " + chatId));
                summaryMessage.setGroup(group);
                summaryMessage.setChat(null);
            } else {
                throw new RuntimeException("Invalid chatId - not found in Chat or Group tables: " + chatId);
            }

            messageRepository.save(summaryMessage);
            log.info("Summary saved successfully for chatId={} by user={}", chatId, userEmail);

        } catch (UsernameNotFoundException e) {
            log.error("User not found while saving summary for chatId={}: {}", chatId, e.getMessage(), e);
        } catch (Exception e) {
            log.error("Failed to save summary as message for chatId={}", chatId, e);
        }
    }

    private boolean isChatId(UUID chatId) {
        return chatRepository.existsById(chatId);
    }

    private boolean isGroupId(UUID chatId) {
        return groupRepository.existsById(chatId);
    }
}
