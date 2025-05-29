package com.shubham.chatsapp.service;

import com.shubham.chatsapp.dto.ChatDTO;
import com.shubham.chatsapp.dto.GroupCreateRequest;
import com.shubham.chatsapp.entity.Chat;
import com.shubham.chatsapp.entity.User;
import com.shubham.chatsapp.repository.ChatRepository;
import com.shubham.chatsapp.repository.UserRepository;
import org.antlr.v4.runtime.misc.LogManager;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

@Service
public class ChatService {

    private UserRepository userRepository;
    private ChatRepository chatRepository;

    public ChatDTO createPersonalChat(String currentUserEmail, String receiverEmail) {
        if (currentUserEmail.equalsIgnoreCase(receiverEmail)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot chat with yourself");
        }


    }

    public ChatDTO createGroupChat(String creatorEmail, GroupCreateRequest request) {
    }

    public List<ChatDTO> getAllChats(String currentUserEmail) {
    }
}
