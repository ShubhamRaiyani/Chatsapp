package com.shubham.chatsapp.repository;


import com.shubham.chatsapp.entity.Chat;
import com.shubham.chatsapp.entity.ChatSummaries;
import com.shubham.chatsapp.entity.Group;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ChatSummaryRepository extends JpaRepository<ChatSummaries, UUID> {

}
