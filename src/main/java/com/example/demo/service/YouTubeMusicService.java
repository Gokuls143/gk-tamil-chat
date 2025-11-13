package com.example.demo.service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

/**
 * YouTube Music Service - Bloomee-style integration
 * Searches YouTube and extracts audio URLs using yt-dlp approach
 */
@Service
public class YouTubeMusicService {

    /**
     * Search YouTube for music tracks
     * Returns list of tracks with playable audio URLs
     */
    public List<Map<String, Object>> searchMusic(String query) {
        List<Map<String, Object>> results = new ArrayList<>();
        
        try {
            // Use yt-dlp to search YouTube (if available on server)
            // Command: yt-dlp "ytsearch5:query" --get-title --get-id --get-duration --get-url --format "bestaudio"
            
            // Alternative: Use YouTube Data API v3 (requires API key)
            // Or use a public YouTube search endpoint
            
            // For immediate functionality, we'll use a hybrid approach:
            // 1. Search YouTube (public endpoint or API)
            // 2. Get video IDs
            // 3. Extract audio URLs using yt-dlp or service
            
            results = this.executeYouTubeSearch(query);
            
        } catch (Exception e) {
            System.err.println("YouTube music search error: " + e.getMessage());
            e.printStackTrace();
        }
        
        return results;
    }

    /**
     * Execute YouTube search using yt-dlp or API
     */
    private List<Map<String, Object>> executeYouTubeSearch(String query) {
        List<Map<String, Object>> results = new ArrayList<>();
        
        try {
            // Check if yt-dlp is available
            if (this.isYtDlpAvailable()) {
                // Use yt-dlp for search and audio extraction
                results = this.searchWithYtDlp(query);
            } else {
                // Fallback: Use YouTube search and return video format
                // Frontend will handle audio extraction
                results = this.searchYouTubeBasic(query);
            }
        } catch (Exception e) {
            System.err.println("Search execution error: " + e.getMessage());
        }
        
        return results;
    }

    /**
     * Check if yt-dlp is installed and available
     */
    private boolean isYtDlpAvailable() {
        try {
            Process process = new ProcessBuilder("yt-dlp", "--version")
                .redirectErrorStream(true)
                .start();
            
            int exitCode = process.waitFor();
            return exitCode == 0;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Search using yt-dlp (Bloomee-style)
     */
    private List<Map<String, Object>> searchWithYtDlp(String query) {
        List<Map<String, Object>> results = new ArrayList<>();
        
        try {
            // yt-dlp command to search and get audio URLs
            ProcessBuilder pb = new ProcessBuilder(
                "yt-dlp",
                "ytsearch5:" + query,  // Search top 5 results
                "--get-title",
                "--get-id",
                "--get-duration",
                "--get-url",
                "--format", "bestaudio/best",
                "--no-playlist",
                "--default-search", "ytsearch"
            );
            
            Process process = pb.start();
            BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream()));
            
            String line;
            Map<String, Object> currentTrack = null;
            int fieldIndex = 0;
            
            while ((line = reader.readLine()) != null) {
                if (fieldIndex % 4 == 0) {
                    // Title
                    if (currentTrack != null) {
                        results.add(currentTrack);
                    }
                    currentTrack = new HashMap<>();
                    currentTrack.put("title", line);
                } else if (fieldIndex % 4 == 1) {
                    // Video ID
                    if (currentTrack != null) {
                        currentTrack.put("videoId", line);
                        currentTrack.put("url", "https://www.youtube.com/watch?v=" + line);
                    }
                } else if (fieldIndex % 4 == 2) {
                    // Duration
                    if (currentTrack != null) {
                        try {
                            int duration = Integer.parseInt(line);
                            currentTrack.put("duration", duration);
                        } catch (NumberFormatException e) {
                            currentTrack.put("duration", 0);
                        }
                    }
                } else if (fieldIndex % 4 == 3) {
                    // Audio URL
                    if (currentTrack != null) {
                        currentTrack.put("audioUrl", line);
                        currentTrack.put("url", line); // Use direct audio URL
                        currentTrack.put("artist", "YouTube"); // Default artist
                    }
                }
                fieldIndex++;
            }
            
            if (currentTrack != null) {
                results.add(currentTrack);
            }
            
            process.waitFor();
            
        } catch (Exception e) {
            System.err.println("yt-dlp search error: " + e.getMessage());
            e.printStackTrace();
        }
        
        return results;
    }

    /**
     * Basic YouTube search (fallback when yt-dlp not available)
     * Returns YouTube video format - frontend handles audio extraction
     */
    private List<Map<String, Object>> searchYouTubeBasic(String query) {
        List<Map<String, Object>> results = new ArrayList<>();
        
        // This is a placeholder - in production, you would:
        // 1. Use YouTube Data API v3
        // 2. Or scrape YouTube search results
        // 3. Or use a service that provides YouTube audio URLs
        
        // For now, return format that works with YouTube embed
        // Frontend can use YouTube iframe API or audio extraction service
        
        // Example structure (replace with actual search):
        Map<String, Object> track = new HashMap<>();
        track.put("title", "Search: " + query);
        track.put("artist", "YouTube");
        track.put("url", "https://www.youtube.com/embed?listType=search&list=" + 
                  URLEncoder.encode(query, StandardCharsets.UTF_8));
        track.put("videoId", ""); // Will be populated by actual search
        track.put("duration", 0);
        results.add(track);
        
        return results;
    }
}

