package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.Environment;

import java.nio.file.Files;
import java.nio.file.Paths;

@SpringBootApplication
public class DemoApplication {
    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(DemoApplication.class, args);
        
        // Railway deployment logging
        Environment env = context.getEnvironment();
        String port = env.getProperty("server.port", "8080");
        String profile = env.getProperty("spring.profiles.active", "default");
        String uploadDir = System.getProperty("java.io.tmpdir") + "uploads/";
        
        System.out.println("=".repeat(50));
        System.out.println("🚀 Chat Application Started Successfully!");
        System.out.println("Profile: " + profile);
        System.out.println("Port: " + port);
        System.out.println("Upload Directory: " + uploadDir);
        System.out.println("Upload Directory Exists: " + Files.exists(Paths.get(uploadDir)));
        System.out.println("Database URL: " + env.getProperty("spring.datasource.url", "Not configured"));
        System.out.println("=".repeat(50));
    }
}
