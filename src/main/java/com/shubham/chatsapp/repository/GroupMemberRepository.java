package com.shubham.chatsapp.repository;


import com.shubham.chatsapp.entity.Group;
import com.shubham.chatsapp.entity.GroupMember;
import com.shubham.chatsapp.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface GroupMemberRepository extends JpaRepository<GroupMember, UUID> {
    List<GroupMember> findByGroup(Group group);

    List<User> findUsersByGroupId(UUID groupId);
    List<GroupMember> findByGroup_Id(UUID groupId);

    Optional<GroupMember> findByGroupAndUser(Group group, User user);
}