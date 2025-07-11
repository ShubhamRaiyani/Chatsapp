package com.shubham.chatsapp.service;

import com.cohere.api.Cohere;
import com.cohere.api.resources.v2.requests.V2ChatRequest;
import com.cohere.api.types.ChatMessageV2;
import com.cohere.api.types.ChatResponse;
import com.cohere.api.types.UserMessage;
import com.cohere.api.types.UserMessageContent;
import com.shubham.chatsapp.entity.ChatSummaries;
import com.shubham.chatsapp.entity.Message;
import com.shubham.chatsapp.repository.MessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class ChatSummaryService {

    @Value("${cohere.api.key}")
    private String apiKey;
    @Autowired
    private MessageRepository messageRepository;

    public ChatSummaryService(MessageRepository messageRepository) {
        this.messageRepository = messageRepository;
    }


    public ChatResponse generateChatSummary(UUID chatId) {

        List<Message> messages = messageRepository.findByChatIdOrGroupId(chatId);

        StringBuilder chatText = new StringBuilder("Summarize the following chat:\n");
        for (Message msg : messages) {
            chatText.append(msg.getSender().getUsername())
                    .append(": ")
                    .append(msg.getContent())
                    .append("\n");
        }

        Cohere cohere = Cohere.builder().token(apiKey).build();
        ChatResponse response = cohere.v2().chat(
                V2ChatRequest.builder()
                        .model("command-r-plus")
                        .messages(List.of(
                                ChatMessageV2.user(
                                        UserMessage.builder()
                                                .content(UserMessageContent.of(chatText.toString()))
                                                .build()
                                )
                        ))
                        .build()
        );

        return response;
}

}
