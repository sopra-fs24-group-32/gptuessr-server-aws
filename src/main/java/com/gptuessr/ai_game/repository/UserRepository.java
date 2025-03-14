package com.gptuessr.ai_game.repository;

import com.gptuessr.ai_game.entity.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends MongoRepository<User, String> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    Optional<User> findByClerkUserId(String clerkUserId);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    boolean existsByClerkUserId(String clerkUserId);
}