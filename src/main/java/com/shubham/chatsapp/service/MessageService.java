package com.shubham.chatsapp.service;

import com.shubham.chatsapp.dto.MessageDTO;
import com.shubham.chatsapp.entity.*;
import com.shubham.chatsapp.enums.StatusType;
import com.shubham.chatsapp.repository.*;

import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
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
    private final MessageStatusRepository messageStatusRepository;
    public MessageService(MessageRepository messageRepository, UserRepository userRepository, ChatRepository chatRepository, GroupRepository groupRepository, MessageStatusRepository messageStatusRepository) {
        this.messageRepository = messageRepository;
        this.userRepository = userRepository;
        this.chatRepository = chatRepository;
        this.groupRepository = groupRepository;
        this.messageStatusRepository = messageStatusRepository;
    }

    @Transactional
    public MessageDTO sendMessage(MessageDTO request ,String senderEmail) { // saving
        User sender = userRepository.findByEmail(senderEmail)
                .orElseThrow(() -> new UsernameNotFoundException("Sender not found"));
        User receiver = userRepository.findByEmail(request.getReceiverEmail())
                .orElseThrow(() -> new UsernameNotFoundException("reciver not found"));
        Message message = new Message();
        message.setContent(request.getContent());
        message.setCreatedAt(Instant.now());
        message.setSender(sender);
        message.setReceiver(receiver);
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
        Message savedMessage = messageRepository.save(message);
        System.out.println(">>> Message saved: " + savedMessage.getId());

        //Here where we mark initial status as SENT
        MessageStatus status = new MessageStatus();
        status.setMessage(savedMessage);
        status.setUser(message.getReceiver());
        status.setStatus(StatusType.SENT);
        status.setUpdatedAt(Instant.now());
        MessageStatus savedStatus = messageStatusRepository.save(status);
        System.out.println(">>> Messagestatus saved: " + savedStatus.getMessage().getId());
        return mapToDTO(savedMessage);

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
        dto.setMessageId(message.getId());
        dto.setContent(message.getContent());
//        dto.setSenderEmail(message.getSender().getEmail());
        dto.setChatId(message.getChat() != null ? message.getChat().getId() : null);
        dto.setGroupId(message.getGroup() != null ? message.getGroup().getId() : null);
        dto.setSentAt(message.getCreatedAt().atZone(ZoneId.systemDefault()).toLocalDateTime());
        dto.setMessageType(message.getMessageType());
        return dto;
    }

    public Message getMessageFromMessageDTO(MessageDTO savedMessageDTO) {
        Message message = messageRepository.findById(savedMessageDTO.getMessageId())
                .orElseThrow(()-> new IllegalArgumentException("Messagedto to message failed"));
        return message;
    }

//    private Message mapDtoToMessage(MessageDTO messageDTO){
//        Message message = new Message();
//        message.set
//    }


}
