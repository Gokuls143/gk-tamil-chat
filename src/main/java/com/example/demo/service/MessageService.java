package com.example.demo.service;

import com.example.demo.model.Message;
import com.example.demo.repository.MessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class MessageService {
    
    @Autowired
    private MessageRepository messageRepository;
    
    /**
     * Get message count for today
     */
    public long getTodayMessageCount() {
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = LocalDate.now().atTime(23, 59, 59);
        return this.messageRepository.countByTimestampBetween(startOfDay, endOfDay);
    }
    
    /**
     * Get all messages
     */
    public List<Message> getAllMessages() {
        return this.messageRepository.findAllByOrderByTimestampDesc();
    }
    
    /**
     * Get messages by user
     */
    public List<Message> getMessagesByUser(String username) {
        return this.messageRepository.findBySenderOrderByTimestampDesc(username);
    }
    
    /**
     * Delete message by ID
     */
    public boolean deleteMessage(Long messageId) {
        try {
            this.messageRepository.deleteById(messageId);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Get message count for last 7 days
     */
    public long getWeeklyMessageCount() {
        LocalDateTime weekAgo = LocalDateTime.now().minusDays(7);
        return this.messageRepository.countByTimestampAfter(weekAgo);
    }
    
    /**
     * Get total message count
     */
    public long getTotalMessageCount() {
        return this.messageRepository.count();
    }
}