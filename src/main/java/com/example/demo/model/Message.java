package com.example.demo.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

@Entity
@Table(name = "messages", indexes = {
    @Index(name = "idx_timestamp", columnList = "timestamp"),
    @Index(name = "idx_sender_timestamp", columnList = "sender, timestamp")
})
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String sender;

    @Column(nullable = false)
    private String content;

    @Column(nullable = false)
    private LocalDateTime timestamp = LocalDateTime.now();

    // Attachment fields
    @Column
    private String attachmentType; // "image", "audio", "gif"
    
    @Column
    private String attachmentUrl; // URL to the uploaded file
    
    @Column
    private String attachmentName; // Original filename
    
    @Column
    private Long attachmentSize; // File size in bytes

    // Quote/Reply fields
    @Column
    private Long quotedMessageId; // ID of the message being quoted
    
    @Column
    private String quotedSender; // Sender of the quoted message
    
    @Column(length = 1000)
    private String quotedContent; // Content of the quoted message

    public Message() {}

    public Message(String sender, String content) {
        this.sender = sender;
        this.content = content;
        this.timestamp = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() { return this.id; }

    public String getSender() { return this.sender; }
    public void setSender(String sender) { this.sender = sender; }

    public String getContent() { return this.content; }
    public void setContent(String content) { this.content = content; }

    public LocalDateTime getTimestamp() { return this.timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    
    // Attachment getters and setters
    public String getAttachmentType() { return this.attachmentType; }
    public void setAttachmentType(String attachmentType) { this.attachmentType = attachmentType; }
    
    public String getAttachmentUrl() { return this.attachmentUrl; }
    public void setAttachmentUrl(String attachmentUrl) { this.attachmentUrl = attachmentUrl; }
    
    public String getAttachmentName() { return this.attachmentName; }
    public void setAttachmentName(String attachmentName) { this.attachmentName = attachmentName; }
    
    public Long getAttachmentSize() { return this.attachmentSize; }
    public void setAttachmentSize(Long attachmentSize) { this.attachmentSize = attachmentSize; }
    
    // Quote/Reply getters and setters
    public Long getQuotedMessageId() { return this.quotedMessageId; }
    public void setQuotedMessageId(Long quotedMessageId) { this.quotedMessageId = quotedMessageId; }
    
    public String getQuotedSender() { return this.quotedSender; }
    public void setQuotedSender(String quotedSender) { this.quotedSender = quotedSender; }
    
    public String getQuotedContent() { return this.quotedContent; }
    public void setQuotedContent(String quotedContent) { this.quotedContent = quotedContent; }
}
