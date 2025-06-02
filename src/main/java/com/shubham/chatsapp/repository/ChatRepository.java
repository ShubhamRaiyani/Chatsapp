package com.shubham.chatsapp.repository;

import com.shubham.chatsapp.entity.Chat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ChatRepository extends JpaRepository<Chat, UUID> {
    @Query("""
    select c from Chat c
left join c.message m
where (m.sender.id = :userId1 and m.receiver.id = :userId2)
or (m.sender.id = :userId2 and m.receiver.id = :userId1)
""")
    Optional<Chat> findPersonalChatBetweenUsers(UUID id, UUID id1);

    List<Chat> findAllPersonalChatsByUserId(UUID id);
}
