package com.example.demo.service;

import com.example.demo.model.Message;
import com.example.demo.repository.MessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

@Service
public class MessageService {
    
    @Autowired
    private MessageRepository messageRepository;
    
    /**
     * Get message count for today (IST timezone)
     */
    public long getTodayMessageCount() {
        ZoneId istZone = ZoneId.of("Asia/Kolkata");
        LocalDate today = ZonedDateTime.now(istZone).toLocalDate();
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = today.atTime(23, 59, 59);
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
     * Get message count for last 7 days (IST timezone)
     */
    public long getWeeklyMessageCount() {
        ZoneId istZone = ZoneId.of("Asia/Kolkata");
        LocalDateTime weekAgo = ZonedDateTime.now(istZone).toLocalDateTime().minusDays(7);
        return this.messageRepository.countByTimestampAfter(weekAgo);
    }
    
    /**
     * Get total message count
     */
    public long getTotalMessageCount() {
        return this.messageRepository.count();
    }
}