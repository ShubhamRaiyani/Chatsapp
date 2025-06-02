package com.shubham.chatsapp.service;

import com.shubham.chatsapp.dto.ChatDTO;
import com.shubham.chatsapp.dto.ChatDetailsDTO;
import com.shubham.chatsapp.dto.GroupCreateRequest;
import com.shubham.chatsapp.entity.*;
import com.shubham.chatsapp.repository.*;
import org.antlr.v4.runtime.misc.LogManager;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.*;
import java.util.stream.Stream;

@Service
public class ChatService {

    private final UserRepository userRepository;
    private final ChatRepository chatRepository;
    private final MessageRepository messageRepository;
    private final GroupRepository groupRepository;
    private final GroupMemberRepository groupMemberRepository;

    public ChatService(UserRepository userRepository, ChatRepository chatRepository,
                       MessageRepository messageRepository, GroupRepository groupRepository,
                       GroupMemberRepository groupMemberRepository) {
        this.userRepository = userRepository;
        this.chatRepository = chatRepository;
        this.messageRepository = messageRepository;
        this.groupRepository = groupRepository;
        this.groupMemberRepository = groupMemberRepository;
    }

    @Transactional
    public ChatDTO createPersonalChat(String currentUserEmail, String otherUserEmail) {
        User user1 = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new RuntimeException("Current user not found"));
        User user2 = userRepository.findByEmail(otherUserEmail)
                .orElseThrow(() -> new RuntimeException("Other user not found"));

        Optional<Chat> existingChat = chatRepository.findPersonalChatBetweenUsers(user1.getId(), user2.getId());
        if (existingChat.isPresent()) {
            return mapChatToDTO(existingChat.get(),currentUserEmail);
        }

        // Create new chat
        Chat chat = new Chat();
        chat.setTimestamp(Instant.now());

        // Create welcome message
        Message welcomeMessage = new Message();
        welcomeMessage.setSender(user1); // optional: use a system sender
        welcomeMessage.setReceiver(user2);
        welcomeMessage.setChat(chat);
        welcomeMessage.setContent("👋 Welcome " + user1.getEmail() + " and " + user2.getEmail() + "!");
        welcomeMessage.setCreatedAt(Instant.now());

        // Add message to chat
        chat.getMessages().add(welcomeMessage);

        // Save chat (cascade saves message too)
        Chat savedChat = chatRepository.save(chat);
        return mapChatToDTO(savedChat,currentUserEmail);
    }

    @Transactional
    public ChatDTO createGroupChat(String creatorEmail, GroupCreateRequest request) {
        User creator = userRepository.findByEmail(creatorEmail)
                .orElseThrow(() -> new RuntimeException("Creator user not found"));

        // Create group
        Group group = new Group();
        group.setName(request.getName());
        group.setCreatedBy(creator);
        group.setCreatedAt(Instant.now());
        Group savedGroup = groupRepository.save(group);

        // Add creator as admin
        GroupMember creatorMember = new GroupMember();
        creatorMember.setGroup(savedGroup);
        creatorMember.setUser(creator);
        creatorMember.setRole("ADMIN");
        creatorMember.setJoinedAt(Instant.now());
        creatorMember.setLastseenAt(Instant.now());
        groupMemberRepository.save(creatorMember);

        // Add other members
        if (request.getMemberIds() != null) {
            for (String email : request.getMemberIds()) {
                User memberUser = userRepository.findByEmail(email)
                        .orElseThrow(() -> new RuntimeException("User with email " + email + " not found"));
                GroupMember member = new GroupMember();
                member.setGroup(savedGroup);
                member.setUser(memberUser);
                member.setRole("MEMBER");
                member.setJoinedAt(Instant.now());
                member.setLastseenAt(Instant.now());
                groupMemberRepository.save(member);
            }
        }

        // Create welcome message in group
        Message welcome = new Message();
        welcome.setSender(creator);
        welcome.setGroup(savedGroup);
        welcome.setContent("Group '" + savedGroup.getName() + "' has been created by " + creator.getEmail());
        welcome.setCreatedAt(Instant.now());
        messageRepository.save(welcome);

        return mapGroupToChatDTO(savedGroup);
    }

    public List<ChatDTO> getAllChats(String currentUserEmail) {
        User user = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<Chat> personalChats = chatRepository.findAllPersonalChatsByUserId(user.getId());
        List<Group> groups = groupRepository.findGroupsByMemberUserId(user.getId());

        List<ChatDTO> chatDTOs = personalChats.stream()
                .map(chat -> mapChatToDTO(chat, currentUserEmail))
                .toList();

        List<ChatDTO> groupChatDTOs = groups.stream()
                .map(this::mapGroupToChatDTO)
                .toList();

        chatDTOs.addAll(groupChatDTOs);
        return chatDTOs;
    }

    private ChatDTO mapChatToDTO(Chat chat, String currentUserEmail) {
        ChatDTO dto = new ChatDTO();
        dto.setId(chat.getId());
        dto.setGroup(false);

        // Find other participant's name
        String otherUserName = chat.getMessages().stream()
                .flatMap(m -> Stream.of(m.getSender(), m.getReceiver()))
                .filter(user -> user != null && !user.getEmail().equals(currentUserEmail))
                .map(User::getUsername)
                .findFirst()
                .orElse("Unknown");

        dto.setDisplayName(otherUserName);

        // Last activity is chat's timestamp or last message createdAt
        Instant lastActivity = chat.getMessages().stream()
                .map(Message::getCreatedAt)
                .max(Comparator.naturalOrder())
                .orElse(chat.getTimestamp());
        dto.setLastActivity(lastActivity);

        return dto;
    }

    private ChatDTO mapGroupToChatDTO(Group group) {
        ChatDTO dto = new ChatDTO();
        dto.setId(group.getId());
        dto.setDisplayName(group.getName());
        dto.setGroup(true);
        dto.setLastActivity(group.getCreatedAt());
        return dto;
    }

    public ChatDetailsDTO getChatDetails(UUID chatId, String currentUserEmail) {
        // Try to find chat first
        Optional<Chat> chatOpt = chatRepository.findById(chatId);
        if (chatOpt.isPresent()) {
            Chat chat = chatOpt.get();

            // Find other participant in personal chat
            String otherUserName = chat.getMessages().stream()
                    .flatMap(m -> Stream.of(m.getSender(), m.getReceiver()))
                    .filter(u -> u != null && !u.getEmail().equals(currentUserEmail))
                    .map(User::getEmail)
                    .findFirst()
                    .orElse("Unknown");

            ChatDetailsDTO dto = new ChatDetailsDTO();
            dto.setChatId(chat.getId());
            dto.setDisplayName(otherUserName);
            dto.setGroup(false);
            dto.setLastActivity(chat.getTimestamp());

            // You can also add participant emails if you want (for personal chat just the 2 users)
            List<String> participants = chat.getMessages().stream()
                    .flatMap(m -> Stream.of(m.getSender(), m.getReceiver()))
                    .filter(Objects::nonNull)
                    .map(User::getEmail)
                    .distinct()
                    .toList();
            dto.setParticipantEmails(participants);

            return dto;
        }

        // If not personal chat, try group
        Optional<Group> groupOpt = groupRepository.findById(chatId);
        if (groupOpt.isPresent()) {
            Group group = groupOpt.get();

            ChatDetailsDTO dto = new ChatDetailsDTO();
            dto.setChatId(group.getId());
            dto.setDisplayName(group.getName());
            dto.setGroup(true);
            dto.setLastActivity(group.getCreatedAt());

            // Add emails of group members
            List<String> memberEmails = groupMemberRepository.findByGroup(group).stream()
                    .map(gm -> gm.getUser().getEmail())
                    .toList();
            dto.setParticipantEmails(memberEmails);

            return dto;
        }

        throw new NoSuchElementException("Chat or Group not found");
    }


}
