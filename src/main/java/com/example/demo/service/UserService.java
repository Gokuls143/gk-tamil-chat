package com.example.demo.service;

import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class UserService {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private UserTrackingService userTrackingService;
    
    /**
     * Get total user count
     */
    public long getTotalUserCount() {
        return this.userRepository.count();
    }
    
    /**
     * Get online user count
     */
    public long getOnlineUserCount() {
        return this.userTrackingService.getOnlineUserCount();
    }
    
    /**
     * Get users with pagination
     */
    public List<Map<String, Object>> getUsersWithPagination(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        Page<User> userPage = this.userRepository.findAll(pageable);
        
        List<Map<String, Object>> users = new ArrayList<>();
        for (User user : userPage.getContent()) {
            Map<String, Object> userMap = new HashMap<>();
            userMap.put("id", user.getId());
            userMap.put("username", user.getUsername());
            userMap.put("email", user.getEmail());
            userMap.put("gender", user.getGender());
            userMap.put("age", user.getAge());
            userMap.put("status", user.getStatus());
            userMap.put("isAdmin", user.getIsAdmin());
            userMap.put("isMuted", user.getIsMuted());
            userMap.put("isBanned", user.getIsBanned());
            userMap.put("isOnline", this.userTrackingService.isUserOnline(user.getUsername()));
            users.add(userMap);
        }
        
        return users;
    }
    
    /**
     * Update user status (active/inactive, muted, banned)
     */
    public boolean updateUserStatus(Long userId, boolean active) {
        Optional<User> userOpt = this.userRepository.findById(userId);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.setIsBanned(!active);
            this.userRepository.save(user);
            
            // If user is being banned, disconnect them
            if (!active) {
                this.userTrackingService.removeUser(user.getUsername());
            }
            
            return true;
        }
        return false;
    }
    
    /**
     * Mute/unmute user
     */
    public boolean muteUser(Long userId, boolean muted) {
        Optional<User> userOpt = this.userRepository.findById(userId);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.setIsMuted(muted);
            this.userRepository.save(user);
            return true;
        }
        return false;
    }
    
    /**
     * Get user by ID
     */
    public User getUserById(Long userId) {
        return this.userRepository.findById(userId).orElse(null);
    }
    
    /**
     * Get user by username
     */
    public User getUserByUsername(String username) {
        return this.userRepository.findByUsername(username);
    }
    
    /**
     * Get user by email
     */
    public User getUserByEmail(String email) {
        return this.userRepository.findByEmail(email);
    }
    
    /**
     * Delete user account
     */
    public boolean deleteUser(Long userId) {
        Optional<User> userOpt = this.userRepository.findById(userId);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            // Remove from online tracking first
            this.userTrackingService.removeUser(user.getUsername());
            // Delete user
            this.userRepository.delete(user);
            return true;
        }
        return false;
    }
    
    /**
     * Search users by username or email
     */
    public List<User> searchUsers(String query) {
        return this.userRepository.findByUsernameContainingIgnoreCaseOrEmailContainingIgnoreCase(query, query);
    }
    
    /**
     * Get banned users
     */
    public List<User> getBannedUsers() {
        return this.userRepository.findByIsBannedTrue();
    }
    
    /**
     * Get muted users
     */
    public List<User> getMutedUsers() {
        return this.userRepository.findByIsMutedTrue();
    }
}