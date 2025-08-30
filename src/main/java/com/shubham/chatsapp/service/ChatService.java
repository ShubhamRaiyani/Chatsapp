package com.shubham.chatsapp.service;

import com.google.gson.internal.GsonTypes;
import com.shubham.chatsapp.dto.ChatDTO;
import com.shubham.chatsapp.dto.ChatDetailsDTO;
import com.shubham.chatsapp.dto.GroupCreateRequest;
import com.shubham.chatsapp.entity.*;
import com.shubham.chatsapp.enums.StatusType;
import com.shubham.chatsapp.repository.*;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Stream;

@Service
@Slf4j
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
        chat.setUser1(user1);
        chat.setUser2(user2);

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
        if (request.getMemberEmails() != null) {
            for (String email : request.getMemberEmails()) {
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

        return mapGroupToChatDTO(savedGroup,creatorEmail);
    }

    public List<ChatDTO> getAllChats(String currentUserEmail) {
        User user = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<Chat> personalChats = chatRepository.findAllPersonalChatsByUserId(user.getId());
        List<Group> groups = groupRepository.findGroupsByUserId(user.getId());

        List<ChatDTO> chatDTOs = new ArrayList<>(personalChats.stream()
                .map(chat -> mapChatToDTO(chat, currentUserEmail))
                .toList());

        List<ChatDTO> groupChatDTOs = groups.stream()
                .map(group -> mapGroupToChatDTO(group ,currentUserEmail))
                .toList();

        chatDTOs.addAll(groupChatDTOs);
        return chatDTOs;
    }

    private ChatDTO mapChatToDTO(Chat chat, String currentUserEmail)    {
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

        //reciever email
        String receiverEmail = null;
        if (chat.getUser1() != null && !chat.getUser1().getEmail().equals(currentUserEmail)) {
            receiverEmail = chat.getUser1().getEmail();
        } else if (chat.getUser2() != null && !chat.getUser2().getEmail().equals(currentUserEmail)) {
            receiverEmail = chat.getUser2().getEmail();
        }
        System.out.println(receiverEmail + "reciever email in mapcaht tot dto");
        dto.setReceiverEmail(receiverEmail);
        // Last activity is chat's timestamp or last message createdAt
        Instant lastActivity = chat.getMessages().stream()
                .map(Message::getCreatedAt)
                .max(Comparator.naturalOrder())
                .orElse(chat.getTimestamp());
        dto.setLastActivity(lastActivity);

        String lastMessage = chat.getMessages().stream()
                .max(Comparator.comparing(Message::getCreatedAt)) // Get the latest message by createdAt
                .map(Message::getContent)                         // Extract its content
                .orElse("");                                      // Fallback if no messages

        dto.setLastMessage(lastMessage);

        long unreadcount = countUnreadMessages(chat.getMessages(), receiverEmail, currentUserEmail);
        dto.setUnreadCount(unreadcount);

        return dto;
    }

    private ChatDTO mapGroupToChatDTO(Group group,String currentUserEmail) {
        ChatDTO dto = new ChatDTO();
        dto.setId(group.getId());
        dto.setDisplayName(group.getName());
        dto.setGroup(true);
        Instant lastActivity = group.getMessages().stream()
                .map(Message::getCreatedAt)
                .max(Comparator.naturalOrder())
                .orElse(group.getCreatedAt());
        dto.setLastActivity(lastActivity);
        String lastMessage = group.getMessages().stream()
                .max(Comparator.comparing(Message::getCreatedAt)) // Get the latest message by createdAt
                .map(Message::getContent)                         // Extract its content
                .orElse("");                                      // Fallback if no messages
        dto.setLastMessage(lastMessage);

        List<String> memberEmails = groupMemberRepository.findByGroup(group).stream()
                .map(gm -> gm.getUser().getEmail())
                .toList();
        dto.setParticipantEmails(memberEmails);
        long unreadcount = countUnreadMessagesInGroup(group.getMessages() , currentUserEmail);
        dto.setUnreadCount(unreadcount);

        return dto;
    }
    // For personal chats
    public long countUnreadMessages(List<Message> messages, String senderEmail, String currentUserEmail) {
        if (senderEmail == null || currentUserEmail == null) {
            return 0; // no valid sender/current user
        }
        return messages.stream()
                // Only messages from the given sender
                .filter(msg -> msg.getSender() != null
                        && senderEmail.equals(msg.getSender().getEmail()))
                // Check the status for the current user
                .filter(msg -> msg.getStatuses() != null && msg.getStatuses().stream()
                        .anyMatch(status -> status.getUser() != null
                                && currentUserEmail.equals(status.getUser().getEmail())
                                && status.getStatus() != StatusType.READ))
                .count();
    }

    // For group chats
    public long countUnreadMessagesInGroup(List<Message> messages, String currentUserEmail) {
        if (currentUserEmail == null) {
            return 0; // no valid current user
        }
        return messages.stream()
                // Message must be sent by someone else
                .filter(msg -> msg.getSender() != null
                        && !currentUserEmail.equals(msg.getSender().getEmail()))
                // Check the status for the current user
                .filter(msg -> msg.getStatuses() != null && msg.getStatuses().stream()
                        .anyMatch(status -> status.getUser() != null
                                && currentUserEmail.equals(status.getUser().getEmail())
                                && status.getStatus() != StatusType.READ))
                .count();
    }







    public ChatDetailsDTO getChatDetails(UUID chatId, String currentUserEmail) {

        log.info("🔍 Fetching chat details for chatId={} and currentUserEmail={}", chatId, currentUserEmail);

        // Try to find chat first
        Optional<Chat> chatOpt = chatRepository.findById(chatId);
        if (chatOpt.isPresent()) {
            Chat chat = chatOpt.get();
            log.debug("✅ Chat found: {}", chat);

            // Find other participant in personal chat
            //            String otherUserName = chat.getMessages().stream()
            //                    .flatMap(m -> Stream.of(m.getSender(), m.getReceiver()))
            //                    .filter(u -> u != null && !u.getEmail().equals(currentUserEmail))
            //                    .map(User::getEmail)
            //                    .findFirst()
            //                    .orElse("Unknown");
            //            log.debug("👤 Other participant (name/email) from messages: {}", otherUserName);

            String receiverEmail = null;
            if (chat.getUser1() != null && !chat.getUser1().getEmail().equals(currentUserEmail)) {
                receiverEmail = chat.getUser1().getEmail();
            } else if (chat.getUser2() != null && !chat.getUser2().getEmail().equals(currentUserEmail)) {
                receiverEmail = chat.getUser2().getEmail();
            }
            log.debug("📩 Receiver email from Chat entity: {}", receiverEmail);

            String username = userRepository.findByEmail(receiverEmail).orElseThrow(() -> {
                throw new UsernameNotFoundException("user not found from the email");
            }).getUsername();

            ChatDetailsDTO dto = new ChatDetailsDTO();
            dto.setChatId(chat.getId());
            dto.setDisplayName(username);
            dto.setGroup(false);
            dto.setReceiverEmail(receiverEmail);
            dto.setLastActivity(chat.getTimestamp());
            // Participants
            List<String> participants = chat.getMessages().stream()
                    .flatMap(m -> Stream.of(m.getSender(), m.getReceiver()))
                    .filter(Objects::nonNull)
                    .map(User::getEmail)
                    .distinct()
                    .toList();
            dto.setParticipantEmails(participants);

            log.info("📋 Returning chat details DTO: {}", dto);
            return dto;
        }

        // If not personal chat, try group
        Optional<Group> groupOpt = groupRepository.findById(chatId);
        if (groupOpt.isPresent()) {
            Group group = groupOpt.get();
            log.debug("👥 Group found: {}", group.getName());

            ChatDetailsDTO dto = new ChatDetailsDTO();
            dto.setChatId(group.getId());
            dto.setDisplayName(group.getName());
            dto.setGroup(true);
            dto.setLastActivity(group.getCreatedAt());

            List<String> memberEmails = groupMemberRepository.findByGroup(group).stream()
                    .map(gm -> gm.getUser().getEmail())
                    .toList();
            dto.setParticipantEmails(memberEmails);

            log.info("📋 Returning group chat details DTO: {}", dto);
            return dto;
        }

        log.error("❌ Chat or Group not found for chatId={}", chatId);
        throw new NoSuchElementException("Chat or Group not found");
    }

    



}
