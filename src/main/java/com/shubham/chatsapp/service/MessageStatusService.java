package com.shubham.chatsapp.service;

import com.shubham.chatsapp.dto.MessageDTO;
import com.shubham.chatsapp.entity.Message;
import com.shubham.chatsapp.entity.MessageStatus;
import com.shubham.chatsapp.entity.User;
import com.shubham.chatsapp.enums.StatusType;
import com.shubham.chatsapp.repository.MessageRepository;
import com.shubham.chatsapp.repository.MessageStatusRepository;
import com.shubham.chatsapp.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class MessageStatusService {

    private final MessageStatusRepository statusRepository;
    private final MessageRepository messageRepository;
    private final UserRepository userRepository;

    public void markDelivered(Message message, User user) {
        updateOrCreateStatus(message, user, StatusType.DELIVERED);
    }

    public void markRead(Message message, User user) {
        updateOrCreateStatus(message, user, StatusType.READ);
    }


    private void updateOrCreateStatus(Message message, User user, StatusType status) {
        Optional<MessageStatus> optional = statusRepository.findByMessageAndUser(message, user);

        if (optional.isPresent()) {
            MessageStatus current = optional.get();
            log.info("found existing status for message {}", message.getId());
            if (status.ordinal() > current.getStatus().ordinal()) {
                current.setStatus(status);
                current.setUpdatedAt(Instant.now());
                statusRepository.save(current);
                System.out.println("saved updated read status");
            }
        } else {
            MessageStatus newStatus = new MessageStatus();
            newStatus.setMessage(message);
            newStatus.setUser(user);
            newStatus.setStatus(status);
            newStatus.setUpdatedAt(Instant.now());
            statusRepository.save(newStatus);
            System.out.println("saved new message read");
        }
    }


    public void markAllMessagesAsRead(UUID chatId, UUID userId) {
        User user = userRepository.findById(userId).orElseThrow(()-> new IllegalArgumentException("user Not found "));
        List<Message> messages = messageRepository.findUnreadMessages(chatId, userId);
//        List<Message> messages = messageRepository.findUnreadMessagesByChatId(chatId, user);

        for (Message message : messages) {
            markRead(message, user);
        }
    }

    @Transactional
    public List<MessageStatus> markMessagesDelivered(String receiverEmail) {
        List<MessageStatus> toDeliver = statusRepository.findByUserEmailAndStatus(receiverEmail, StatusType.SENT);

        for (MessageStatus status : toDeliver) {
            status.setStatus(StatusType.DELIVERED);
            log.info("marked delivered ");
        }

        return statusRepository.saveAll(toDeliver); // Also returns list to notify senders
    }



}

