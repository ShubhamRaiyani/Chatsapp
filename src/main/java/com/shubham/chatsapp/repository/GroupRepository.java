package com.shubham.chatsapp.repository;

import com.shubham.chatsapp.entity.Group;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface GroupRepository extends JpaRepository<Group, UUID> {
    List<Group> findGroupsByMemberUserId(UUID id);
}