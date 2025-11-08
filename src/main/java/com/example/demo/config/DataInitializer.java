package com.example.demo.config;

import com.example.demo.service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements ApplicationRunner {
    
    @Autowired
    private AdminService adminService;
    
    @Override
    public void run(ApplicationArguments args) throws Exception {
        // Create initial super admin if no admins exist
        this.adminService.createInitialSuperAdmin();
    }
}