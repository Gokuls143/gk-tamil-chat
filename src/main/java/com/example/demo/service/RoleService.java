package com.example.demo.service;
import java.time.temporal.ChronoUnit;
import com.example.demo.repository.MessageRepository;
import java.time.temporal.ChronoUnit;

import com.example.demo.model.User;
import com.example.demo.model.UserRole;
import com.example.demo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class RoleService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MessageRepository messageRepository;
    // Automatic role progression: promote NEW_MEMBER to MEMBER after 10 messages and 7 days
    public void checkAndPromoteUser(User user) {
        if (user.getUserRole() == UserRole.NEW_MEMBER && user.getCreatedAt() != null) {
            long daysSinceJoin = ChronoUnit.DAYS.between(user.getCreatedAt(), java.time.LocalDateTime.now());
            long messageCount = this.messageRepository.countBySender(user.getUsername());
            if (daysSinceJoin >= 7 && messageCount >= 10L) {
                user.setUserRole(UserRole.MEMBER);
                user.setRoleAssignedAt(java.time.LocalDateTime.now());
                user.setRoleAudit("Auto-promoted to MEMBER after activity threshold.");
                this.userRepository.save(user);
                // Optionally: notify user of promotion
            }
        }
    }

    public boolean assignRole(Long userId, UserRole newRole, String auditNote) {
        Optional<User> userOpt = this.userRepository.findById(userId);
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                String previousRole = user.getUserRole() != null ? user.getUserRole().name() : "NONE";
                user.setUserRole(newRole);
                user.setRoleAssignedAt(LocalDateTime.now());
                user.setRoleAudit((auditNote != null ? auditNote : "") + " | Changed from " + previousRole + " to " + newRole.name() + " at " + LocalDateTime.now());
                user.setUpdatedAt(LocalDateTime.now());
                this.userRepository.save(user);
                // Optionally: log to external system or file
                return true;
            }
            return false;
    }

    public UserRole getUserRole(Long userId) {
        return this.userRepository.findById(userId)
                .map(User::getUserRole)
                .orElse(null);
    }
}
