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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

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

    /**
     * Fetches messages from the last 2 days for the given chat or group,
     * sends them to Cohere’s Summarize endpoint, and saves the summary.
     *
     * @param chatId    UUID of the chat or group
     * @param userEmail Email of the requesting user
     * @return the generated summary text, or null if no messages to summarize
     */
    public String generateChatSummaryText(UUID chatId, String userEmail) {
        LocalDateTime twoDaysAgo = LocalDateTime.now().minusDays(2);
        List<Message> messages = messageRepository.findRecentMessages(chatId, twoDaysAgo);
        if (messages.isEmpty()) {
            return null;
        }

        StringBuilder sb = new StringBuilder("Summarize the following chat give only summary no other details :\n");
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
//                                ChatMessageV2.system(
//                                        SystemMessage.builder()
//                                                .content(SystemMessageContent.of("You are a helpful assistant that summarizes chats. one paragraph"))
//                                                .build()
//                                ),
                                ChatMessageV2.user(
                                        UserMessage.builder()
                                                .content(UserMessageContent.of(prompt))
                                                .build()
                                )
                        ))
                        .build()
        );

        // Now extract just the summary text
        if (response != null
                && response.getMessage() != null
                && response.getMessage().getContent() != null
                && !response.getMessage().getContent().isEmpty()) {

            // e.g. first content piece
            // depending on SDK, content objects might have getText() or get("text")
            String summaryText = response.getMessage().getContent().get().get(0).getText().get().getText();
            // Save the summary as a message if response is successful
            saveSummaryAsMessage(chatId, userEmail, summaryText);
            return summaryText;
        } else {
            return null;
        }
    }

    /**
     * Persists the generated summary as a Message entity under the given chat or group.
     *
     * @param chatId         UUID of the chat or group
     * @param userEmail      Email of the requesting user
     * @param summaryContent The summary text to save
     */
    private void saveSummaryAsMessage(UUID chatId, String userEmail, String summaryContent) {
        if (summaryContent == null || summaryContent.trim().isEmpty()) {
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
        } catch (Exception e) {
            System.err.println("Failed to save summary as message: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private boolean isChatId(UUID chatId) {
        return chatRepository.existsById(chatId);
    }

    private boolean isGroupId(UUID chatId) {
        return groupRepository.existsById(chatId);
    }
}
