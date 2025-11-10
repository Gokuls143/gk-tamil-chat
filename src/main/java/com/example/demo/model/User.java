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
    private Boolean isMuted = false;
    public Boolean getIsMuted() { return this.isMuted; }
    public void setIsMuted(Boolean isMuted) { this.isMuted = isMuted; }
    private Boolean isBanned = false;
    private Boolean isAdmin = false;
    private Boolean isSuperAdmin = false;
    public Boolean getIsBanned() { return this.isBanned; }
    public void setIsBanned(Boolean isBanned) { this.isBanned = isBanned; }

    public Boolean getIsAdmin() { return this.isAdmin; }
    public void setIsAdmin(Boolean isAdmin) { this.isAdmin = isAdmin; }

    public Boolean getIsSuperAdmin() { return this.isSuperAdmin; }
    public void setIsSuperAdmin(Boolean isSuperAdmin) { this.isSuperAdmin = isSuperAdmin; }
    @Column(name = "message_count")
    private Integer messageCount = 0;

    @Column(name = "last_login_at")
    private java.time.LocalDateTime lastLoginAt;

    @Column(name = "login_count")
    private Integer loginCount = 0;

    public Integer getMessageCount() { return this.messageCount; }
    public void setMessageCount(Integer messageCount) { this.messageCount = messageCount; }

    public java.time.LocalDateTime getLastLoginAt() { return this.lastLoginAt; }
    public void setLastLoginAt(java.time.LocalDateTime lastLoginAt) { this.lastLoginAt = lastLoginAt; }

    public Integer getLoginCount() { return this.loginCount; }
    public void setLoginCount(Integer loginCount) { this.loginCount = loginCount; }
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
    
    // Role system fields
    @Column(nullable = false)
    private UserRole userRole = UserRole.NEW_MEMBER;

    @Column(name = "role_assigned_at")
    private java.time.LocalDateTime roleAssignedAt;

    @Column(name = "created_at", updatable = false)
    private java.time.LocalDateTime createdAt;

    @Column(name = "updated_at")
    private java.time.LocalDateTime updatedAt;

    @Column(name = "role_audit", length = 1000)
    private String roleAudit;

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
    
    public UserRole getUserRole() { return this.userRole; }
    public void setUserRole(UserRole userRole) { this.userRole = userRole; }

    public java.time.LocalDateTime getRoleAssignedAt() { return this.roleAssignedAt; }
    public void setRoleAssignedAt(java.time.LocalDateTime roleAssignedAt) { this.roleAssignedAt = roleAssignedAt; }

    public java.time.LocalDateTime getCreatedAt() { return this.createdAt; }
    public void setCreatedAt(java.time.LocalDateTime createdAt) { this.createdAt = createdAt; }

    public java.time.LocalDateTime getUpdatedAt() { return this.updatedAt; }
    public void setUpdatedAt(java.time.LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public String getRoleAudit() { return this.roleAudit; }
    public void setRoleAudit(String roleAudit) { this.roleAudit = roleAudit; }
}
