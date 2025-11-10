package com.example.demo.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.demo.model.Admin;
import com.example.demo.repository.AdminRepository;

@Service
public class AdminService {
    
    @Autowired
    private AdminRepository adminRepository;
    
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    
    /**
     * Authenticate admin login
     */
    public Admin authenticateAdmin(String username, String password) {
        Optional<Admin> adminOpt = this.adminRepository.findByUsername(username);
        
        if (adminOpt.isPresent()) {
            Admin admin = adminOpt.get();
            if (admin.isActive() && this.passwordEncoder.matches(password, admin.getPassword())) {
                // Update last login
                admin.setLastLogin(LocalDateTime.now());
                this.adminRepository.save(admin);
                return admin;
            }
        }
        return null;
    }
    
    /**
     * Find admin by username
     */
    public Admin findByUsername(String username) {
        return this.adminRepository.findByUsername(username).orElse(null);
    }
    
    /**
     * Find admin by email
     */
    public Admin findByEmail(String email) {
        return this.adminRepository.findByEmail(email).orElse(null);
    }
    
    /**
     * Create new admin (only by super admin)
     */
    public Admin createAdmin(String username, String password, String email, String role) {
        // Check if admin already exists
        if (this.adminRepository.existsByUsername(username)) {
            throw new RuntimeException("Admin with username already exists");
        }
        
        if (email != null && !email.trim().isEmpty() && this.adminRepository.existsByEmail(email)) {
            throw new RuntimeException("Admin with email already exists");
        }
        
        // Validate role
        if (role == null || (!role.equals("ADMIN") && !role.equals("SUPER_ADMIN"))) {
            role = "ADMIN";
        }
        
        // Create new admin
        Admin admin = new Admin();
        admin.setUsername(username.trim());
        admin.setPassword(this.passwordEncoder.encode(password));
        admin.setEmail(email != null ? email.trim() : null);
        admin.setRole(role);
        admin.setActive(true);
        admin.setCreatedAt(LocalDateTime.now());
        
        return this.adminRepository.save(admin);
    }
    
    /**
     * Get all admins
     */
    public List<Admin> getAllAdmins() {
        return this.adminRepository.findAllActiveAdmins();
    }
    
    /**
     * Get total admin count
     */
    public long getTotalAdminCount() {
        return this.adminRepository.countActiveAdmins();
    }
    
    /**
     * Delete admin (only by super admin)
     */
    public boolean deleteAdmin(Long adminId) {
        Optional<Admin> adminOpt = this.adminRepository.findById(adminId);
        if (adminOpt.isPresent()) {
            // Soft delete - set as inactive
            Admin admin = adminOpt.get();
            admin.setActive(false);
            this.adminRepository.save(admin);
            return true;
        }
        return false;
    }
    
    /**
     * Update admin password
     */
    public boolean updatePassword(Long adminId, String oldPassword, String newPassword) {
        Optional<Admin> adminOpt = this.adminRepository.findById(adminId);
        if (adminOpt.isPresent()) {
            Admin admin = adminOpt.get();
            if (this.passwordEncoder.matches(oldPassword, admin.getPassword())) {
                admin.setPassword(this.passwordEncoder.encode(newPassword));
                this.adminRepository.save(admin);
                return true;
            }
        }
        return false;
    }
    
    /**
     * Create initial super admin if no admins exist
     */
    public void createInitialSuperAdmin() {
        // Initial super admin creation removed. Please create admin via DB migration or admin UI.
    }
}