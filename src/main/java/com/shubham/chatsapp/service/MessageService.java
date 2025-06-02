package com.shubham.chatsapp.service;

import com.shubham.chatsapp.dto.MessageDTO;
import com.shubham.chatsapp.entity.Chat;
import com.shubham.chatsapp.entity.Group;
import com.shubham.chatsapp.entity.Message;
import com.shubham.chatsapp.entity.User;
import com.shubham.chatsapp.repository.ChatRepository;
import com.shubham.chatsapp.repository.GroupRepository;
import com.shubham.chatsapp.repository.MessageRepository;
import com.shubham.chatsapp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.ZoneId;
import java.util.UUID;
@Service
@RequiredArgsConstructor
public class MessageService {

//    GET /api/messages/chat/{chatId}?page=0&size=20
//            → Returns page of personal chat messages.
//
//    GET /api/messages/group/{groupId}?page=1&size=10
//            → Returns page 1 of group chat messages.

    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final ChatRepository chatRepository;
    private final GroupRepository groupRepository;

    public MessageDTO sendMessage(MessageDTO request, String senderEmail) {
        User sender = userRepository.findByEmail(senderEmail)
                .orElseThrow(() -> new UsernameNotFoundException("Sender not found"));

        Message message = new Message();
        message.setContent(request.getContent());
        message.setCreatedAt(Instant.now());
        message.setSender(sender);
        message.setMessageType(request.getMessageType());

        if (request.getChatId() != null) {
            Chat chat = chatRepository.findById(UUID.fromString(String.valueOf(request.getChatId())))
                    .orElseThrow(() -> new IllegalArgumentException("Chat not found"));
            message.setChat(chat);
        } else if (request.getGroupId() != null) {
            Group group = groupRepository.findById(UUID.fromString(String.valueOf(request.getGroupId())))
                    .orElseThrow(() -> new IllegalArgumentException("Group not found"));
            message.setGroup(group);
        } else {
            throw new IllegalArgumentException("chatId or groupId must be provided");
        }

        Message saved = messageRepository.save(message);
        return mapToDTO(saved);
    }

    public Page<MessageDTO> getMessagesForChat(UUID chatId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Message> messagePage = messageRepository.findByChat_IdOrderByCreatedAtDesc(chatId, pageable);
        return messagePage.map(this::mapToDTO);
    }

    public Page<MessageDTO> getMessagesForGroup(UUID groupId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Message> messagePage = messageRepository.findByGroup_IdOrderByCreatedAtDesc(groupId, pageable);
        return messagePage.map(this::mapToDTO);
    }
//    public List<MessageDTO> getMessagesForChat(UUID chatId, int page, int size) {
//        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
//        return messageRepository.findByChat_Id(chatId, pageable)
//                .stream()
//                .map(this::mapToDTO)
//                .toList();
//    }
//
//    public List<MessageDTO> getMessagesForGroup(UUID groupId, int page, int size) {
//        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
//        return messageRepository.findByGroup_Id(groupId, pageable)
//                .stream()
//                .map(this::mapToDTO)
//                .toList();
//    }
    private MessageDTO mapToDTO(Message message) {
        MessageDTO dto = new MessageDTO();
        dto.setContent(message.getContent());
        dto.setSenderUsername(message.getSender().getUsername()); // or .getUsername()
        dto.setChatId(
                message.getChat() != null ? message.getChat().getId() :
                message.getGroup() != null ? message.getGroup().getId() : null);
        dto.setSentAt(message.getCreatedAt().atZone(ZoneId.systemDefault()).toLocalDateTime());
        dto.setMessageType("TEXT"); // Or whatever logic you want
        return dto;
    }
    
}
