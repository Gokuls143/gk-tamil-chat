package com.example.demo.service;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;

@Service
public class UserTrackingService {

    private final Set<String> connectedUsers = ConcurrentHashMap.newKeySet();
    private final UserRepository userRepository;

    public UserTrackingService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public void addUser(String username) {
        this.connectedUsers.add(username);
    }

    public void removeUser(String username) {
        this.connectedUsers.remove(username);
    }

    public List<User> getOnlineUsers() {
        if (this.connectedUsers.isEmpty()) {
            return Collections.emptyList(); // avoid calling repository with an empty IN ()
        }
        return this.userRepository.findByUsernameIn(this.connectedUsers);
    }
    
    public Set<String> getConnectedUsernames() {
        return Collections.unmodifiableSet(this.connectedUsers);
    }
    
    public long getOnlineUserCount() {
        return this.connectedUsers.size();
    }
    
    public boolean isUserOnline(String username) {
        return this.connectedUsers.contains(username);
    }
}
