package com.shubham.chatsapp.repository;

import com.shubham.chatsapp.entity.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface MessageRepository extends JpaRepository<Message, UUID> {
    // For personal chat
    Page<Message> findByChat_IdOrderByCreatedAtDesc(UUID chatId, Pageable pageable);

    // For group chat
    Page<Message> findByGroup_IdOrderByCreatedAtDesc(UUID groupId, Pageable pageable);
}