package com.example.demo.repository;

import com.example.demo.model.Admin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AdminRepository extends JpaRepository<Admin, Long> {
    
    Optional<Admin> findByUsername(String username);
    
    Optional<Admin> findByEmail(String email);
    
    boolean existsByUsername(String username);
    
    boolean existsByEmail(String email);
    
    @Query("SELECT COUNT(a) FROM Admin a WHERE a.active = true")
    long countActiveAdmins();
    
    @Query("SELECT a FROM Admin a WHERE a.active = true ORDER BY a.createdAt DESC")
    java.util.List<Admin> findAllActiveAdmins();
}