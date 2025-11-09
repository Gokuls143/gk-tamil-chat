package com.example.demo.dto;

import java.time.LocalDateTime;
import com.example.demo.model.Message;
import com.example.demo.model.User;

public class MessageDTO {
    private Long id;
    private String sender;
    private String content;
    private LocalDateTime timestamp;
    private String attachmentType;
    private String attachmentUrl;
    private String attachmentName;
    private Long attachmentSize;
    
    // User avatar information
    private String senderAvatar;
    
    // Quote/Reply information
    private Long quotedMessageId;
    private String quotedSender;
    private String quotedContent;
    private QuotedMessageInfo quotedMessage; // Nested info for quoted message details
    
    public static class QuotedMessageInfo {
        private Long id;
        private String sender;
        private String content;
        private String senderAvatar;
        
        public QuotedMessageInfo(Long id, String sender, String content, String senderAvatar) {
            this.id = id;
            this.sender = sender;
            this.content = content;
            this.senderAvatar = senderAvatar;
        }
        
        // Getters
        public Long getId() { return this.id; }
        public String getSender() { return this.sender; }
        public String getContent() { return this.content; }
        public String getSenderAvatar() { return this.senderAvatar; }
    }
    
    public MessageDTO() {}
    
    public MessageDTO(Message message, User user) {
        this.id = message.getId();
        this.sender = message.getSender();
        this.content = message.getContent();
        this.timestamp = message.getTimestamp();
        this.attachmentType = message.getAttachmentType();
        this.attachmentUrl = message.getAttachmentUrl();
        this.attachmentName = message.getAttachmentName();
        this.attachmentSize = message.getAttachmentSize();
        
        // Set quote/reply information
        this.quotedMessageId = message.getQuotedMessageId();
        this.quotedSender = message.getQuotedSender();
        this.quotedContent = message.getQuotedContent();
        
        // Set avatar - use default if user not found or no profile picture
        if (user != null && user.getProfilePicture() != null && !user.getProfilePicture().trim().isEmpty()) {
            this.senderAvatar = user.getProfilePicture();
        } else {
            // Generate a default avatar URL based on username
            this.senderAvatar = this.generateDefaultAvatar(message.getSender());
        }
        
        // Build quoted message info if this is a reply
        if (this.quotedMessageId != null && this.quotedSender != null && this.quotedContent != null) {
            String quotedAvatar = this.generateDefaultAvatar(this.quotedSender);
            this.quotedMessage = new QuotedMessageInfo(this.quotedMessageId, this.quotedSender, this.quotedContent, quotedAvatar);
        }
    }
    
    private String generateDefaultAvatar(String username) {
        // Use a service like UI Avatars for default avatars
        String name = username != null ? username.replaceAll("[^a-zA-Z0-9]", "").substring(0, Math.min(username.length(), 2)) : "AN";
        return "https://ui-avatars.com/api/?name=" + name + "&background=random&size=40&rounded=true";
    }
    
    // Getters and Setters
    public Long getId() { return this.id; }
    public void setId(Long id) { this.id = id; }
    
    public String getSender() { return this.sender; }
    public void setSender(String sender) { this.sender = sender; }
    
    public String getContent() { return this.content; }
    public void setContent(String content) { this.content = content; }
    
    public LocalDateTime getTimestamp() { return this.timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    
    public String getAttachmentType() { return this.attachmentType; }
    public void setAttachmentType(String attachmentType) { this.attachmentType = attachmentType; }
    
    public String getAttachmentUrl() { return this.attachmentUrl; }
    public void setAttachmentUrl(String attachmentUrl) { this.attachmentUrl = attachmentUrl; }
    
    public String getAttachmentName() { return this.attachmentName; }
    public void setAttachmentName(String attachmentName) { this.attachmentName = attachmentName; }
    
    public Long getAttachmentSize() { return this.attachmentSize; }
    public void setAttachmentSize(Long attachmentSize) { this.attachmentSize = attachmentSize; }
    
    public String getSenderAvatar() { return this.senderAvatar; }
    public void setSenderAvatar(String senderAvatar) { this.senderAvatar = senderAvatar; }
    
    // Quote/Reply getters and setters
    public Long getQuotedMessageId() { return this.quotedMessageId; }
    public void setQuotedMessageId(Long quotedMessageId) { this.quotedMessageId = quotedMessageId; }
    
    public String getQuotedSender() { return this.quotedSender; }
    public void setQuotedSender(String quotedSender) { this.quotedSender = quotedSender; }
    
    public String getQuotedContent() { return this.quotedContent; }
    public void setQuotedContent(String quotedContent) { this.quotedContent = quotedContent; }
    
    public QuotedMessageInfo getQuotedMessage() { return this.quotedMessage; }
    public void setQuotedMessage(QuotedMessageInfo quotedMessage) { this.quotedMessage = quotedMessage; }
}