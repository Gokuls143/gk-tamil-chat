
package com.example.demo.controller;
import com.example.demo.service.PermissionService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.demo.model.Admin;
import com.example.demo.service.AdminService;
import com.example.demo.service.UserService;
import com.example.demo.service.MessageService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

@RestController
@RequestMapping("/api/admin")
public class AdminAuthController {
    @Autowired
    private PermissionService permissionService;

    @Autowired
    private AdminService adminService;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private MessageService messageService;
    
    private static final String ADMIN_SESSION_KEY = "admin_session";

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody Map<String, String> loginData, 
                                                     HttpServletRequest request) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            String username = loginData.get("username");
            String password = loginData.get("password");
            
            if (username == null || password == null || 
                username.trim().isEmpty() || password.trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "Username and password are required");
                return ResponseEntity.badRequest().body(response);
            }
            
            Admin admin = this.adminService.authenticateAdmin(username, password);
            
            if (admin != null) {
                // Create admin session
                HttpSession session = request.getSession(true);
                session.setAttribute(ADMIN_SESSION_KEY, admin.getUsername());
                session.setAttribute("admin_role", admin.getRole());
                session.setAttribute("admin_id", admin.getId());
                session.setMaxInactiveInterval(24 * 60 * 60); // 24 hours
                
                response.put("success", true);
                response.put("message", "Login successful");
                response.put("username", admin.getUsername());
                response.put("role", admin.getRole());
                response.put("isSuperAdmin", "SUPER_ADMIN".equals(admin.getRole()));
                
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "Invalid username or password");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Login failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/session/check")
    public ResponseEntity<Map<String, Object>> checkSession(HttpServletRequest request) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            HttpSession session = request.getSession(false);
            
            if (session != null) {
                String adminUsername = (String) session.getAttribute(ADMIN_SESSION_KEY);
                String adminRole = (String) session.getAttribute("admin_role");
                
                if (adminUsername != null) {
                    // Verify admin still exists and is active
                    Admin admin = this.adminService.findByUsername(adminUsername);
                    if (admin != null && admin.isActive()) {
                        response.put("authenticated", true);
                        response.put("username", adminUsername);
                        response.put("role", adminRole);
                        response.put("isSuperAdmin", "SUPER_ADMIN".equals(adminRole));
                        return ResponseEntity.ok(response);
                    }
                }
            }
            
            response.put("authenticated", false);
            response.put("message", "No valid admin session");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            
        } catch (Exception e) {
            response.put("authenticated", false);
            response.put("message", "Session check failed");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getStatus(HttpServletRequest request) {
        // Delegate to the existing session check logic
        return this.checkSession(request);
    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, Object>> logout(HttpServletRequest request) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            HttpSession session = request.getSession(false);
            if (session != null) {
                session.invalidate();
            }
            
            response.put("success", true);
            response.put("message", "Logged out successfully");
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Logout failed");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats(HttpServletRequest request) {
        // Check admin authentication
        if (!this.isAdminAuthenticated(request)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        Map<String, Object> stats = new HashMap<>();
        
        try {
            // Get user statistics
            long totalUsers = this.userService.getTotalUserCount();
            long onlineUsers = this.userService.getOnlineUserCount();
            
            // Get admin statistics
            long totalAdmins = this.adminService.getTotalAdminCount();
            
            // Get message statistics
            long todayMessages = this.messageService.getTodayMessageCount();
            
            stats.put("totalUsers", totalUsers);
            stats.put("onlineUsers", onlineUsers);
            stats.put("totalAdmins", totalAdmins);
            stats.put("todayMessages", todayMessages);
            
            return ResponseEntity.ok(stats);
            
        } catch (Exception e) {
            stats.put("error", "Failed to load statistics");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(stats);
        }
    }

    @GetMapping("/users")
    public ResponseEntity<?> getUsers(HttpServletRequest request,
                                     @RequestParam(defaultValue = "0") int page,
                                     @RequestParam(defaultValue = "50") int size) {
        // Check admin authentication
        if (!this.isAdminAuthenticated(request)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        try {
            List<Map<String, Object>> users = this.userService.getUsersWithPagination(page, size);
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to load users"));
        }
    }

    @GetMapping("/admins")
    public ResponseEntity<?> getAdmins(HttpServletRequest request) {
        // Check admin authentication
        if (!this.isAdminAuthenticated(request)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        try {
            List<Admin> admins = this.adminService.getAllAdmins();
            return ResponseEntity.ok(admins);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to load admins"));
        }
    }

    @PostMapping("/admins")
    public ResponseEntity<Map<String, Object>> createAdmin(@RequestBody Map<String, String> adminData,
                                                           HttpServletRequest request) {
        Map<String, Object> response = new HashMap<>();
        
        // Check if user is super admin
        if (!this.isSuperAdmin(request)) {
            response.put("success", false);
            response.put("message", "Only super admins can create new admins");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
        }
        
        try {
            String username = adminData.get("username");
            String password = adminData.get("password");
            String email = adminData.get("email");
            String role = adminData.get("role");
            
            if (username == null || password == null || username.trim().isEmpty() || password.trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "Username and password are required");
                return ResponseEntity.badRequest().body(response);
            }
            
            // Check if admin already exists
            if (this.adminService.findByUsername(username) != null) {
                response.put("success", false);
                response.put("message", "Admin with this username already exists");
                return ResponseEntity.badRequest().body(response);
            }
            
            Admin newAdmin = this.adminService.createAdmin(username, password, email, role);
            
            response.put("success", true);
            response.put("message", "Admin created successfully");
            response.put("admin", Map.of(
                "id", newAdmin.getId(),
                "username", newAdmin.getUsername(),
                "email", newAdmin.getEmail(),
                "role", newAdmin.getRole()
            ));
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to create admin: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @DeleteMapping("/admins/{id}")
    public ResponseEntity<Map<String, Object>> deleteAdmin(@PathVariable Long id,
                                                           HttpServletRequest request) {
        Map<String, Object> response = new HashMap<>();
        
        // Check if user is super admin
        if (!this.isSuperAdmin(request)) {
            response.put("success", false);
            response.put("message", "Only super admins can delete admins");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
        }
        
        try {
            // Prevent deleting self
            HttpSession session = request.getSession(false);
            Long currentAdminId = (Long) session.getAttribute("admin_id");
            
            if (id.equals(currentAdminId)) {
                response.put("success", false);
                response.put("message", "Cannot delete your own admin account");
                return ResponseEntity.badRequest().body(response);
            }
            
            boolean deleted = this.adminService.deleteAdmin(id);
            
            if (deleted) {
                response.put("success", true);
                response.put("message", "Admin deleted successfully");
            } else {
                response.put("success", false);
                response.put("message", "Admin not found");
            }
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to delete admin: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PutMapping("/users/{id}/status")
    public ResponseEntity<Map<String, Object>> updateUserStatus(@PathVariable Long id,
                                                                @RequestBody Map<String, Boolean> statusData,
                                                                HttpServletRequest request) {
        Map<String, Object> response = new HashMap<>();
        
        // Check admin authentication
        if (!this.isAdminAuthenticated(request)) {
            response.put("success", false);
            response.put("message", "Unauthorized");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
        
        try {
            Boolean active = statusData.get("active");
            if (active == null) {
                response.put("success", false);
                response.put("message", "Status is required");
                return ResponseEntity.badRequest().body(response);
            }
            
            boolean updated = this.userService.updateUserStatus(id, active);
            
            if (updated) {
                response.put("success", true);
                response.put("message", active ? "User activated" : "User deactivated");
            } else {
                response.put("success", false);
                response.put("message", "User not found");
            }
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to update user status: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    private boolean isAdminAuthenticated(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            String adminUsername = (String) session.getAttribute(ADMIN_SESSION_KEY);
            return adminUsername != null;
        }
        return false;
    }

    private boolean isSuperAdmin(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            String adminRole = (String) session.getAttribute("admin_role");
            return "SUPER_ADMIN".equals(adminRole);
        }
        return false;
    }

    @PutMapping("/update-password")
    public ResponseEntity<Map<String, Object>> updatePassword(@RequestBody Map<String, String> passwordData,
                                                               HttpServletRequest request) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Check if user is authenticated as admin
            if (!this.isAdminAuthenticated(request)) {
                response.put("success", false);
                response.put("message", "Admin authentication required");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }
            
            String oldPassword = passwordData.get("oldPassword");
            String newPassword = passwordData.get("newPassword");
            String confirmPassword = passwordData.get("confirmPassword");
            
            if (oldPassword == null || newPassword == null || confirmPassword == null ||
                oldPassword.trim().isEmpty() || newPassword.trim().isEmpty() || confirmPassword.trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "All password fields are required");
                return ResponseEntity.badRequest().body(response);
            }
            
            if (!newPassword.equals(confirmPassword)) {
                response.put("success", false);
                response.put("message", "New password and confirm password do not match");
                return ResponseEntity.badRequest().body(response);
            }
            
            if (newPassword.length() < 6) {
                response.put("success", false);
                response.put("message", "New password must be at least 6 characters long");
                return ResponseEntity.badRequest().body(response);
            }
            
            // Get current admin's ID from session
            HttpSession session = request.getSession(false);
            Long adminId = (Long) session.getAttribute("admin_id");
            
            if (adminId == null) {
                response.put("success", false);
                response.put("message", "Admin session invalid");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }
            
            boolean updated = this.adminService.updatePassword(adminId, oldPassword, newPassword);
            
            if (updated) {
                response.put("success", true);
                response.put("message", "Password updated successfully");
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "Current password is incorrect");
                return ResponseEntity.badRequest().body(response);
            }
            
        } catch (Exception e) {
            String errorMsg = "Password update failed: " + e.getMessage();
            System.err.println("❌ " + errorMsg);
            response.put("success", false);
            response.put("message", "Error updating password: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}