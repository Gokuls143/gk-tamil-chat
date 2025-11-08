package com.example.demo.controller;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class LoginController {
    private static final Logger log = LoggerFactory.getLogger(LoginController.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JavaMailSender mailSender; // may be null

    public LoginController(UserRepository userRepository,
                           PasswordEncoder passwordEncoder,
                           @Autowired(required = false) JavaMailSender mailSender) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.mailSender = mailSender; // null if mail not configured
    }

    @PostMapping("/register")
    public ResponseEntity<String> registerUser(@RequestBody User user) {
        if (user == null || user.getUsername() == null || user.getPassword() == null || user.getEmail() == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Username, email and password are required");
        }
        
        if (user.getGender() == null || user.getGender().trim().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Gender is required");
        }
        
        if (user.getAge() == null || user.getAge() < 1 || user.getAge() > 150) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Valid age is required");
        }

        String username = user.getUsername().trim().toLowerCase();
        String email = user.getEmail().trim().toLowerCase();
        if (!email.contains("@") || !email.contains(".")) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid email address");
        }

        if (this.userRepository.findByUsername(username) != null) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Username already registered");
        }
        if (this.userRepository.findByEmail(email) != null) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Email already registered");
        }

        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(this.passwordEncoder.encode(user.getPassword()));
        this.userRepository.save(user);
        log.debug("Registered user={}", username);
        return ResponseEntity.status(HttpStatus.CREATED).body("User registered successfully");
    }

    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody Map<String, String> loginRequest, 
                                      jakarta.servlet.http.HttpSession session,
                                      jakarta.servlet.http.HttpServletRequest request) {
        if (loginRequest == null || loginRequest.get("email") == null || loginRequest.get("password") == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Email and password are required");
        }

        String email = loginRequest.get("email").trim().toLowerCase();
        String password = loginRequest.get("password");
        
        User existingUser = this.userRepository.findByEmail(email);
        if (existingUser == null) {
            log.debug("User not found with email: {}", email);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid email or password");
        }

        boolean ok = this.passwordEncoder.matches(password, existingUser.getPassword());
        log.debug("Password match for {}: {}", email, ok);
        if (ok) {
            // Check if there's already a different user logged in this session
            Object currentSessionUser = session.getAttribute("username");
            if (currentSessionUser != null && !existingUser.getUsername().equals(currentSessionUser)) {
                // Different user is already logged in - invalidate current session and create new one
                log.info("Different user ({}) already logged in session. Switching to new user: {}", 
                         currentSessionUser, existingUser.getUsername());
                session.invalidate();
                // Get new session after invalidation
                session = request.getSession(true);
            }
            
            try {
                session.setAttribute("username", existingUser.getUsername());
            } catch (Exception ignore) {}
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "Login successful");
            response.put("username", existingUser.getUsername());
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid email or password");
        }
    }

    @GetMapping("/session/me")
    public ResponseEntity<String> currentUser(jakarta.servlet.http.HttpSession session) {
        Object u = session == null ? null : session.getAttribute("username");
        if (u == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("");
        return ResponseEntity.ok(String.valueOf(u));
    }
    
    @GetMapping("/session/check")
    public ResponseEntity<?> checkExistingSession(jakarta.servlet.http.HttpSession session) {
        Object currentUser = session == null ? null : session.getAttribute("username");
        Map<String, Object> response = new HashMap<>();
        response.put("hasActiveSession", currentUser != null);
        if (currentUser != null) {
            response.put("username", currentUser.toString());
        }
        return ResponseEntity.ok(response);
    }

    @PostMapping("/forgot-username")
    public ResponseEntity<String> forgotUsername(@RequestBody Map<String, String> body) {
        String email = body == null ? null : body.get("email");
        if (email == null || email.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Email required");
        }
        String normalized = email.trim().toLowerCase();
        User user = this.userRepository.findByEmail(normalized);
        if (user != null && this.mailSender != null) {
            SimpleMailMessage msg = new SimpleMailMessage();
            msg.setTo(normalized);
            msg.setSubject("Your username");
            msg.setText("Hello,\n\nYour username: " + user.getUsername() + "\n\nIf you didn't request this, ignore.");
            this.mailSender.send(msg);
        } else if (user != null) {
            log.info("Mail sender not configured; would send username to {}", normalized);
        }
        // Always respond 200 to avoid account enumeration
        return ResponseEntity.ok("If the email is registered, we sent instructions.");
    }

    @PostMapping("/api/forgot-password")
    public ResponseEntity<String> forgotPassword(@RequestBody Map<String, String> body) {
        String email = body == null ? null : body.get("email");
        if (email == null || email.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Email required");
        }
        String normalized = email.trim().toLowerCase();
        User user = this.userRepository.findByEmail(normalized);
        if (user != null && this.mailSender != null) {
            SimpleMailMessage msg = new SimpleMailMessage();
            msg.setTo(normalized);
            msg.setSubject("Password Reset Request");
            msg.setText("Hello " + user.getUsername() + ",\n\n" +
                "You requested a password reset. Please contact the administrator to reset your password.\n\n" +
                "Your username: " + user.getUsername() + "\n\n" +
                "If you didn't request this, please ignore this email.");
            this.mailSender.send(msg);
            log.info("Password reset email sent to {}", normalized);
        } else if (user != null) {
            log.info("Mail sender not configured; would send password reset to {}", normalized);
        }
        // Always respond 200 to avoid account enumeration
        return ResponseEntity.ok("If the email is registered, you will receive password reset instructions.");
    }

    @GetMapping("/users/exists")
    public ResponseEntity<Boolean> usernameExists(@RequestParam("username") String username) {
        if (username == null || username.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(true);
        }
        boolean exists = this.userRepository.findByUsername(username.trim().toLowerCase()) != null;
        return ResponseEntity.ok(exists);
    }

    @PostMapping("/api/logout")
    public ResponseEntity<String> logout(jakarta.servlet.http.HttpSession session) {
        if (session != null) {
            session.invalidate();
        }
        return ResponseEntity.ok("Logged out successfully");
    }
}
