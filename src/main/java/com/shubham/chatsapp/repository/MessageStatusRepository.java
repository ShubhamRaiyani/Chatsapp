package com.shubham.chatsapp.repository;

import com.shubham.chatsapp.entity.Message;
import com.shubham.chatsapp.entity.MessageStatus;
import com.shubham.chatsapp.entity.User;
import com.shubham.chatsapp.enums.StatusType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MessageStatusRepository extends JpaRepository<MessageStatus, UUID>{

    Optional<MessageStatus> findByMessageAndUser(Message message, User user);

    List<MessageStatus> findByUserEmailAndStatus(String receiverEmail, StatusType statusType);
}
