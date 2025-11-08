package com.example.demo.repository;

import java.util.List;
import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.model.User;

public interface UserRepository extends JpaRepository<User, Long> {
    List<User> findByUsernameIn(Set<String> usernames);
    User findByUsername(String username);
    User findByEmail(String email);
    
    // Admin queries
    long countByIsAdminTrue();
    long countByIsMutedTrue();
    long countByIsBannedTrue();
    List<User> findByIsAdminTrue();
    List<User> findByIsSuperAdminTrue();
    
    // Additional admin methods
    List<User> findByIsBannedTrue();
    List<User> findByIsMutedTrue();
    List<User> findByUsernameContainingIgnoreCaseOrEmailContainingIgnoreCase(String username, String email);
}
