package com.shubham.chatsapp.repository;

import com.shubham.chatsapp.entity.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;  
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MessageRepository extends JpaRepository<Message, UUID> {

    // For personal chat
    Page<Message> findByChat_IdOrderByCreatedAtDesc(UUID chatId, Pageable pageable);

    // For group chat
    Page<Message> findByGroup_IdOrderByCreatedAtDesc(UUID groupId, Pageable pageable);

    Optional<Message> findById(UUID messageId);


    @Query("""
        SELECT m FROM Message m
        WHERE m.chat.id = :id OR m.group.id = :id
    """)
    List<Message> findByChatIdOrGroupId(@Param("id") UUID id);

    @Query("SELECT m FROM Message m " +
            "LEFT JOIN m.statuses s ON s.user.id = :userId " +
            "WHERE m.chat.id = :chatId AND m.sender.id <> :userId " +
            "AND (s IS NULL OR s.status <> 'READ')")
    List<Message> findUnreadMessages(@Param("chatId") UUID chatId, @Param("userId") UUID userId);

}
