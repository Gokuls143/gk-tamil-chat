package com.example.demo.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

@Entity
@Table(name = "users", indexes = @Index(columnList = "email", name = "ux_users_email"))
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;
    private String password;

    @Column(nullable = false, length = 255)
    private String email;
    
    private String gender;
    private Integer age;
    
    @Column(length = 100)
    private String status;
    
    @Column(length = 500)
    private String description;
    
    @Column(length = 1000)
    private String story;
    
    @Column(columnDefinition = "LONGTEXT")
    private String profilePicture;
    
    // Admin role fields
    @Column(nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean isAdmin = false;
    
    @Column(nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE") 
    private Boolean isMuted = false;
    
    @Column(nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean isBanned = false;
    
    @Column(nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean isSuperAdmin = false;

    // getters & setters
    public Long getId() { return this.id; }
    public void setId(Long id) { this.id = id; }

    public String getUsername() { return this.username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return this.password; }
    public void setPassword(String password) { this.password = password; }

    public String getEmail() { return this.email; }
    public void setEmail(String email) { this.email = email; }
    
    public String getGender() { return this.gender; }
    public void setGender(String gender) { this.gender = gender; }
    
    public Integer getAge() { return this.age; }
    public void setAge(Integer age) { this.age = age; }
    
    public String getStatus() { return this.status; }
    public void setStatus(String status) { this.status = status; }
    
    public String getDescription() { return this.description; }
    public void setDescription(String description) { this.description = description; }
    
    public String getStory() { return this.story; }
    public void setStory(String story) { this.story = story; }
    
    public String getProfilePicture() { return this.profilePicture; }
    public void setProfilePicture(String profilePicture) { this.profilePicture = profilePicture; }
    
    public Boolean getIsAdmin() { return this.isAdmin != null ? this.isAdmin : false; }
    public void setIsAdmin(Boolean isAdmin) { this.isAdmin = isAdmin; }
    
    public Boolean getIsMuted() { return this.isMuted != null ? this.isMuted : false; }
    public void setIsMuted(Boolean isMuted) { this.isMuted = isMuted; }
    
    public Boolean getIsBanned() { return this.isBanned != null ? this.isBanned : false; }
    public void setIsBanned(Boolean isBanned) { this.isBanned = isBanned; }
    
    public Boolean getIsSuperAdmin() { return this.isSuperAdmin != null ? this.isSuperAdmin : false; }
    public void setIsSuperAdmin(Boolean isSuperAdmin) { this.isSuperAdmin = isSuperAdmin; }
}
