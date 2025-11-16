package com.example.demo.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import com.example.demo.model.User;
import com.example.demo.model.LoginRequest;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.SessionManagementService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class LoginController {

    private static final Logger logger = LoggerFactory.getLogger(LoginController.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JavaMailSender mailSender; 
    private final SessionManagementService sessionService;

    public LoginController(UserRepository userRepository,
                           PasswordEncoder passwordEncoder,
                           @Autowired(required = false) JavaMailSender mailSender,
                           SessionManagementService sessionService) {

        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.mailSender = mailSender;
        this.sessionService = sessionService;
    }

    /* -------------------------------------------------------------------------
     * REGISTER
     * ------------------------------------------------------------------------- */
    @PostMapping("/register")
    public ResponseEntity<String> registerUser(@RequestBody User user) {

        if (user == null ||
            user.getUsername() == null ||
            user.getPassword() == null ||
            user.getEmail() == null) {
            return ResponseEntity.badRequest().body("Username, email and password are required");
        }

        if (user.getGender() == null || user.getGender().trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Gender is required");
        }

        if (user.getAge() == null || user.getAge() < 1 || user.getAge() > 150) {
            return ResponseEntity.badRequest().body("Valid age is required");
        }

        String username = user.getUsername().trim().toLowerCase();
        String email = user.getEmail().trim().toLowerCase();

        if (!email.contains("@") || !email.contains(".")) {
            return ResponseEntity.badRequest().body("Invalid email address");
        }

        if (userRepository.findByUsername(username) != null) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Username already registered");
        }
        if (userRepository.findByEmail(email) != null) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Email already registered");
        }

        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userRepository.save(user);

        logger.info("User registered: {}", username);
        return ResponseEntity.status(HttpStatus.CREATED).body("User registered successfully");
    }

    /* -------------------------------------------------------------------------
     * LOGIN
     * ------------------------------------------------------------------------- */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest req, HttpServletRequest request) {

        if (req == null || req.getEmail() == null || req.getPassword() == null) {
            return ResponseEntity.badRequest().body("Email and password required");
        }

        String email = req.getEmail().trim().toLowerCase();

        logger.info("Login attempt for email={}", email);

        User existingUser = userRepository.findByEmail(email);
        if (existingUser == null) {
            logger.info("Login failed: user not found {}", email);
            return ResponseEntity.status(401).body("Invalid credentials");
        }

        if (!passwordEncoder.matches(req.getPassword(), existingUser.getPassword())) {
            logger.info("Login failed: invalid password for {}", email);
            return ResponseEntity.status(401).body("Invalid credentials");
        }

        if (existingUser.getIsBanned()) {
            logger.info("BANNED user attempted login: {}", email);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Account has been banned. Contact admin.");
        }

        HttpSession session = request.getSession(true);
        String username = existingUser.getUsername();

        // Check if session belongs to different user
        Object currentSessionUser = session.getAttribute("username");
        if (currentSessionUser != null && !username.equals(currentSessionUser)) {

            sessionService.removeSession(session.getId());
            try { session.invalidate(); } catch (Exception ignored) {}

            session = request.getSession(true);
            logger.info("New session created for {}", username);
        }

        sessionService.registerUserSession(username, session);

        // Auto promote super admin (your custom logic)
        if ("popcorn".equals(username) || "gokulkannans92@gmail.com".equals(existingUser.getEmail())) {
            if (!existingUser.getIsSuperAdmin()) {
                existingUser.setIsSuperAdmin(true);
                existingUser.setIsAdmin(true);
                userRepository.save(existingUser);
            }
        }

        session.setAttribute("username", username);
        logger.info("User {} logged in, session={}", username, session.getId());

        Map<String, String> response = new HashMap<>();
        response.put("message", "Login successful");
        response.put("username", username);
        response.put("isAdmin", existingUser.getIsAdmin().toString());
        response.put("isSuperAdmin", existingUser.getIsSuperAdmin().toString());

        return ResponseEntity.ok(response);
    }

    /* -------------------------------------------------------------------------
     * SESSION: CURRENT USER
     * ------------------------------------------------------------------------- */
    @GetMapping("/session/me")
    public ResponseEntity<?> currentUser(HttpSession session) {

        if (session == null || session.getAttribute("username") == null)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("");

        String username = String.valueOf(session.getAttribute("username"));
        User user = userRepository.findByUsername(username);

        if (user == null) {
            session.invalidate();
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("");
        }

        if (user.getIsBanned()) {
            session.invalidate();
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Account banned");
        }

        return ResponseEntity.ok(username);
    }

    /* -------------------------------------------------------------------------
     * CHECK ACTIVE SESSION
     * ------------------------------------------------------------------------- */
    @GetMapping("/session/check")
    public ResponseEntity<?> checkExistingSession(HttpSession session) {

        Map<String, Object> response = new HashMap<>();
        Object currentUser = session == null ? null : session.getAttribute("username");

        response.put("hasActiveSession", currentUser != null);
        if (currentUser != null) {
            response.put("username", currentUser.toString());
        }

        return ResponseEntity.ok(response);
    }

    /* -------------------------------------------------------------------------
     * LOGOUT
     * ------------------------------------------------------------------------- */
    @PostMapping("/logout")
    public ResponseEntity<String> logout(HttpSession session) {

        if (session != null) {
            Object username = session.getAttribute("username");

            if (username != null) {
                sessionService.removeSession(session.getId());
                logger.info("User {} logged out from session={}", username, session.getId());
            }

            session.invalidate();
        }

        return ResponseEntity.ok("Logged out successfully");
    }

    /* -------------------------------------------------------------------------
     * FORGOT USERNAME
     * ------------------------------------------------------------------------- */
    @PostMapping("/forgot-username")
    public ResponseEntity<String> forgotUsername(@RequestBody Map<String, String> body) {

        String email = (body == null) ? null : body.get("email");
        if (email == null || email.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Email required");
        }

        email = email.trim().toLowerCase();
        User user = userRepository.findByEmail(email);

        if (user != null && mailSender != null) {

            SimpleMailMessage msg = new SimpleMailMessage();
            msg.setTo(email);
            msg.setSubject("Your Username");
            msg.setText("Hello,\n\nYour username is: " + user.getUsername());

            mailSender.send(msg);
        }

        return ResponseEntity.ok("If the email is registered, we sent instructions.");
    }

    /* -------------------------------------------------------------------------
     * FORGOT PASSWORD
     * ------------------------------------------------------------------------- */
    @PostMapping("/forgot-password")
    public ResponseEntity<String> forgotPassword(@RequestBody Map<String, String> body) {

        String email = (body == null) ? null : body.get("email");
        if (email == null || email.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Email required");
        }

        email = email.trim().toLowerCase();
        User user = userRepository.findByEmail(email);

        if (user != null && mailSender != null) {

            SimpleMailMessage msg = new SimpleMailMessage();
            msg.setTo(email);
            msg.setSubject("Password Reset");
            msg.setText("Hello " + user.getUsername() + ",\n\n" +
                "Please contact administrator to reset your password.\n\n" +
                "Your username: " + user.getUsername());

            mailSender.send(msg);
        }

        return ResponseEntity.ok("If the email is registered, you will receive instructions.");
    }

    /* -------------------------------------------------------------------------
     * CHECK USERNAME EXISTS
     * ------------------------------------------------------------------------- */
    @GetMapping("/users/exists")
    public ResponseEntity<Boolean> usernameExists(@RequestParam("username") String username) {

        if (username == null || username.trim().isEmpty()) {
            return ResponseEntity.ok(false);
        }

        boolean exists =
            userRepository.findByUsername(username.trim().toLowerCase()) != null;

        return ResponseEntity.ok(exists);
    }
}
