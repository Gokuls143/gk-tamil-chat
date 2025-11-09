package com.example.demo.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Railway-optimized file serving with fallback strategy
        String uploadPath;
        String railwayVolume = System.getenv("RAILWAY_VOLUME_MOUNT_PATH");
        
        if (railwayVolume != null) {
            // Use Railway persistent volume if available
            uploadPath = "file:///" + railwayVolume + "/uploads/";
            System.out.println("Using Railway persistent volume: " + railwayVolume);
        } else {
            // Fallback to system temp directory
            uploadPath = "file:///" + System.getProperty("java.io.tmpdir") + "uploads/";
            System.out.println("Using system temp directory for uploads");
        }
        
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(uploadPath)
                .setCachePeriod(1800) // 30 minutes cache for Railway performance
                .resourceChain(true);
                
        // Serve static resources with longer cache for production
        registry.addResourceHandler("/static/**")
                .addResourceLocations("classpath:/static/")
                .setCachePeriod(1800) // 30 minutes cache
                .resourceChain(false);
                
        // Serve sounds with cache
        registry.addResourceHandler("/sounds/**")
                .addResourceLocations("classpath:/static/sounds/")
                .setCachePeriod(3600); // 1 hour cache for audio files
                
        System.out.println("Configured upload path: " + uploadPath);
    }
}