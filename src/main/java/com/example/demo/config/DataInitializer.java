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
        try {
            System.out.println("Initializing data...");
            // Create initial super admin if no admins exist
            this.adminService.createInitialSuperAdmin();
            System.out.println("Data initialization completed successfully.");
        } catch (Exception e) {
            System.err.println("Data initialization failed: " + e.getMessage());
            // Don't rethrow - allow application to continue
            e.printStackTrace();
        }
    }
}