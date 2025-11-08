package com.example.demo.controller;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.demo.model.Message;
import com.example.demo.repository.MessageRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

@RestController
@RequestMapping("/api")
public class FileUploadController {

    @Autowired
    private MessageRepository messageRepository;
    
    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    // File size limits (in bytes)
    private static final long MAX_IMAGE_SIZE = 5 * 1024 * 1024; // 5MB for images
    private static final long MAX_AUDIO_SIZE = 10 * 1024 * 1024; // 10MB for audio
    private static final long MAX_GIF_SIZE = 8 * 1024 * 1024; // 8MB for GIFs
    
    // Allowed file types
    private static final List<String> ALLOWED_IMAGE_TYPES = Arrays.asList(
        "image/jpeg", "image/jpg", "image/png", "image/webp"
    );
    private static final List<String> ALLOWED_AUDIO_TYPES = Arrays.asList(
        "audio/mpeg", "audio/mp3", "audio/wav", "audio/ogg", "audio/m4a"
    );
    private static final List<String> ALLOWED_GIF_TYPES = Arrays.asList(
        "image/gif"
    );
    
    // Upload directory - Use system temp for Railway compatibility
    private static final String UPLOAD_DIR = System.getProperty("java.io.tmpdir") + "uploads" + System.getProperty("file.separator");

    @PostMapping("/upload")
    public ResponseEntity<String> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam("sender") String sender) {
        
        System.out.println("Upload request received - File: " + 
            (file != null ? file.getOriginalFilename() + " (" + file.getSize() + " bytes)" : "null") + 
            ", Sender: " + sender);
            
        try {
            // Validate file
            if (file == null || file.isEmpty()) {
                System.out.println("Error: File is null or empty");
                return ResponseEntity.badRequest().body("File is empty");
            }
            
            String contentType = file.getContentType();
            System.out.println("File content type: " + contentType);
            
            String fileType = this.getFileType(contentType);
            long fileSize = file.getSize();
            
            System.out.println("Detected file type: " + fileType + ", Size: " + fileSize);
            
            if (fileType == null) {
                System.out.println("Error: Unsupported file type - " + contentType);
                return ResponseEntity.badRequest()
                    .body("Unsupported file type. Only images (JPEG, PNG, WebP), GIFs, and audio (MP3, WAV, OGG, M4A) are allowed.");
            }
            
            // Check file size limits
            if (!this.isValidFileSize(fileType, fileSize)) {
                System.out.println("Error: File too large - " + fileSize + " bytes for type " + fileType);
                return ResponseEntity.badRequest()
                    .body("File too large. Max sizes: Images/GIFs: 8MB, Audio: 10MB");
            }
            
            // Create upload directory if it doesn't exist - Railway-safe
            Path uploadPath = Paths.get(UPLOAD_DIR);
            try {
                if (!Files.exists(uploadPath)) {
                    Files.createDirectories(uploadPath);
                    System.out.println("Created upload directory: " + uploadPath.toAbsolutePath());
                }
            } catch (Exception e) {
                System.err.println("Failed to create upload directory: " + e.getMessage());
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to initialize file storage");
            }
            
            // Generate unique filename
            String originalFilename = file.getOriginalFilename();
            if (originalFilename == null || originalFilename.isEmpty()) {
                originalFilename = "file." + this.getFileExtension(contentType);
            }
            String fileExtension = originalFilename.contains(".") ? 
                originalFilename.substring(originalFilename.lastIndexOf(".")) : 
                "." + this.getFileExtension(contentType);
            String uniqueFilename = UUID.randomUUID().toString() + fileExtension;
            Path filePath = uploadPath.resolve(uniqueFilename);
            
            // Save file
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
            
            // Create message with attachment
            Message message = new Message();
            message.setSender(sender != null ? sender : "anonymous");
            message.setContent("📎 Shared " + this.getFileTypeDisplayName(fileType));
            message.setAttachmentType(fileType);
            message.setAttachmentUrl("/uploads/" + uniqueFilename);
            message.setAttachmentName(originalFilename);
            message.setAttachmentSize(fileSize);
            
            Message savedMessage = this.messageRepository.save(message);
            
            // Broadcast the message via WebSocket
            this.messagingTemplate.convertAndSend("/topic/messages", savedMessage);
            
            // Return JSON response
            ObjectMapper mapper = new ObjectMapper();
            return ResponseEntity.ok(mapper.writeValueAsString(savedMessage));
            
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Failed to upload file: " + e.getMessage());
        }
    }
    
    private String getFileType(String contentType) {
        if (ALLOWED_IMAGE_TYPES.contains(contentType)) {
            return "image";
        } else if (ALLOWED_GIF_TYPES.contains(contentType)) {
            return "gif";
        } else if (ALLOWED_AUDIO_TYPES.contains(contentType)) {
            return "audio";
        }
        return null;
    }
    
    private boolean isValidFileSize(String fileType, long fileSize) {
        switch (fileType) {
            case "image":
                return fileSize <= MAX_IMAGE_SIZE;
            case "gif":
                return fileSize <= MAX_GIF_SIZE;
            case "audio":
                return fileSize <= MAX_AUDIO_SIZE;
            default:
                return false;
        }
    }
    
    private String getFileTypeDisplayName(String fileType) {
        switch (fileType) {
            case "image":
                return "an image";
            case "gif":
                return "a GIF";
            case "audio":
                return "an audio file";
            default:
                return "a file";
        }
    }
    
    private String getFileExtension(String contentType) {
        switch (contentType) {
            case "image/jpeg":
            case "image/jpg":
                return "jpg";
            case "image/png":
                return "png";
            case "image/webp":
                return "webp";
            case "image/gif":
                return "gif";
            case "audio/mpeg":
            case "audio/mp3":
                return "mp3";
            case "audio/wav":
                return "wav";
            case "audio/ogg":
                return "ogg";
            case "audio/m4a":
                return "m4a";
            default:
                return "bin";
        }
    }
}