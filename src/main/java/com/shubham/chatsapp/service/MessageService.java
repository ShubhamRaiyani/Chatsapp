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
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class MessageService {

    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final ChatRepository chatRepository;
    private final GroupRepository groupRepository;
    private final MessageStatusRepository messageStatusRepository;
    private final GroupMemberRepository groupMemberRepository; // ✅ Correct repository

    public MessageService(MessageRepository messageRepository, UserRepository userRepository,
                          ChatRepository chatRepository, GroupRepository groupRepository,
                          MessageStatusRepository messageStatusRepository,
                          GroupMemberRepository groupMemberRepository) {
        this.messageRepository = messageRepository;
        this.userRepository = userRepository;
        this.chatRepository = chatRepository;
        this.groupRepository = groupRepository;
        this.messageStatusRepository = messageStatusRepository;
        this.groupMemberRepository = groupMemberRepository;
    }

    @Transactional
    public MessageDTO sendMessage(MessageDTO request, String senderEmail) {
        User sender = userRepository.findByEmail(senderEmail)
                .orElseThrow(() -> new UsernameNotFoundException("Sender not found"));

        Message message = new Message();
        message.setContent(request.getContent());
        message.setCreatedAt(LocalDateTime.now());
        message.setSender(sender);
        message.setMessageType(request.getMessageType());

        System.out.println(">>> sendMessage() called for: " + senderEmail);

        if (request.getChatId() != null) {
            // Handle Direct Message
            User receiver = userRepository.findByEmail(request.getReceiverEmail())
                    .orElseThrow(() -> new UsernameNotFoundException("Receiver not found"));

            Chat chat = chatRepository.findById(request.getChatId())
                    .orElseThrow(() -> new IllegalArgumentException("Chat not found"));

            message.setChat(chat);
            message.setReceiver(receiver);

            Message savedMessage = messageRepository.save(message);
            System.out.println(">>> Direct message saved: " + savedMessage.getId());

            // Create single status for direct message
            createDirectMessageStatus(savedMessage, receiver);

            return mapToDTO(savedMessage);

        } else if (request.getGroupId() != null) {
            // Handle Group Message
            Group group = groupRepository.findById(request.getGroupId())
                    .orElseThrow(() -> new IllegalArgumentException("Group not found"));

            message.setGroup(group);
            // No single receiver for group messages

            Message savedMessage = messageRepository.save(message);
            System.out.println(">>> Group message saved: " + savedMessage.getId());

            // Create status for all group members except sender
            createGroupMessageStatuses(savedMessage, group, sender);

            return mapToDTO(savedMessage);

        } else {
            throw new IllegalArgumentException("Either chatId or groupId must be provided");
        }
    }

    /**
     * Create message status for direct message (single receiver)
     */
    private void createDirectMessageStatus(Message message, User receiver) {
        MessageStatus status = new MessageStatus();
        status.setMessage(message);
        status.setUser(receiver);
        status.setStatus(StatusType.SENT);
        status.setUpdatedAt(Instant.now());

        messageStatusRepository.save(status);
        System.out.println(">>> Direct message status created for: " + receiver.getEmail());
    }

    /**
     * Create message statuses for group message (all members except sender)
     */
    private void createGroupMessageStatuses(Message message, Group group, User sender) {
        List<User> groupMembers = getGroupMembers(group.getId());

        System.out.println(">>> Creating statuses for " + groupMembers.size() + " group members");

        for (User member : groupMembers) {
            // Skip the sender - they don't need a status for their own message
            if (!member.getId().equals(sender.getId())) {
                MessageStatus status = new MessageStatus();
                status.setMessage(message);
                status.setUser(member);
                status.setStatus(StatusType.SENT);
                status.setUpdatedAt(Instant.now());

                messageStatusRepository.save(status);
                System.out.println(">>> Group message status created for: " + member.getEmail());
            }
        }
    }

    /**
     * ✅ CORRECTED: Get all members of a group using GroupMember junction table
     */
    public List<User> getGroupMembers(UUID groupId) {
        // Get all GroupMember entities for this group
        List<GroupMember> groupMembers = groupMemberRepository.findByGroup_Id(groupId);

        // Extract User entities from GroupMember entities
        return groupMembers.stream()
                .map(GroupMember::getUser)
                .collect(Collectors.toList());
    }

    // Keep your existing methods unchanged
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

    private MessageDTO mapToDTO(Message message) {
        MessageDTO dto = new MessageDTO();
        dto.setMessageId(message.getId());
        dto.setContent(message.getContent());
        dto.setChatId(message.getChat() != null ? message.getChat().getId() : null);
        dto.setGroupId(message.getGroup() != null ? message.getGroup().getId() : null);
        dto.setSentAt(message.getCreatedAt().atZone(ZoneId.systemDefault()).toLocalDateTime());
        dto.setSenderEmail(message.getSender().getEmail());
        dto.setMessageType(message.getMessageType());

        // Add receiver email for direct messages only
        if (message.getReceiver() != null) {
            dto.setReceiverEmail(message.getReceiver().getEmail());
        }

        return dto;
    }

    public Message getMessageFromMessageDTO(MessageDTO savedMessageDTO) {
        return messageRepository.findById(savedMessageDTO.getMessageId())
                .orElseThrow(() -> new IllegalArgumentException("Message not found"));
    }
}
