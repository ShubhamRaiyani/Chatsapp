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

import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.ZoneId;
import java.util.UUID;

@Service
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

    public MessageService(MessageRepository messageRepository, UserRepository userRepository, ChatRepository chatRepository, GroupRepository groupRepository) {
        this.messageRepository = messageRepository;
        this.userRepository = userRepository;
        this.chatRepository = chatRepository;
        this.groupRepository = groupRepository;
    }

    @Transactional
    public MessageDTO sendMessage(MessageDTO request) {
        User sender = userRepository.findByEmail(request.getSenderEmail())
                .orElseThrow(() -> new UsernameNotFoundException("Sender not found"));

        Message message = new Message();
        message.setContent(request.getContent());
        message.setCreatedAt(Instant.now());
        message.setSender(sender);
        message.setMessageType(request.getMessageType());
        System.out.println(">>>>> sendMessage() called with email: ");
        if (request.getChatId() != null) {
            Chat chat = chatRepository.findById(request.getChatId())
                    .orElseThrow(() -> new IllegalArgumentException("Chat not found"));
            message.setChat(chat);
        } else if (request.getGroupId() != null) {
            Group group = groupRepository.findById(request.getGroupId())
                    .orElseThrow(() -> new IllegalArgumentException("Group not found"));
            message.setGroup(group);
        } else {
            throw new IllegalArgumentException("chatId or groupId must be provided");
        }
        System.out.println(">>> Saving message to chat: " + message.getChat());
        Message saved = messageRepository.save(message);
        System.out.println(">>> Message saved: " + saved.getId());
        return mapToDTO(saved);

    }


    public Page<MessageDTO> getMessagesForChat(UUID chatId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        System.out.println(pageable);
        Page<Message> messagePage = messageRepository.findByChat_IdOrderByCreatedAtDesc(chatId, pageable);
        System.out.println(messagePage);
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
        dto.setSenderEmail(message.getSender().getEmail());
        dto.setChatId(message.getChat() != null ? message.getChat().getId() : null);
        dto.setGroupId(message.getGroup() != null ? message.getGroup().getId() : null);
        dto.setSentAt(message.getCreatedAt().atZone(ZoneId.systemDefault()).toLocalDateTime());
        dto.setMessageType(message.getMessageType());
        return dto;
    }


}
