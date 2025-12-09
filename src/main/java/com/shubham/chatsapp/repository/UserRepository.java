package com.shubham.chatsapp.repository;

import com.shubham.chatsapp.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByEmail(String email);

    List<User> findByEmailContainingIgnoreCaseAndEnabledTrue(String emailFragment);

    @Query("SELECT u FROM User u WHERE " +
            "LOWER(SUBSTRING(u.email, 1, POSITION('@' IN u.email) - 1)) " +
            "LIKE CONCAT('%', LOWER(:usernameFragment), '%') " +
            "AND u.enabled = true")
    List<User> findByEmailUsernameContainingAndEnabledTrue(@Param("usernameFragment") String usernameFragment);
}