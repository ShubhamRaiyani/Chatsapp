package com.shubham.chatsapp.repository;

import com.shubham.chatsapp.entity.Chat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ChatRepository extends JpaRepository<Chat, UUID> {
    @Query("""
    SELECT c FROM Chat c
    LEFT JOIN FETCH c.messages
    WHERE :user1Id IN (
        SELECT m.sender.id FROM Message m WHERE m.chat = c
    )
    AND :user2Id IN (
        SELECT m.receiver.id FROM Message m WHERE m.chat = c
    )
""")
    Optional<Chat> findPersonalChatBetweenUsers(@Param("user1Id") UUID user1Id, @Param("user2Id") UUID user2Id);


    @Query("""
    SELECT DISTINCT c FROM Chat c
    JOIN c.messages m
    WHERE m.sender.id = :userId OR m.receiver.id = :userId
""")
    List<Chat> findAllPersonalChatsByUserId(@Param("userId") UUID userId);
}
