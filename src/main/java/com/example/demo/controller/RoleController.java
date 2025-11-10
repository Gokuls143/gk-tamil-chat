package com.example.demo.controller;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.enums.UserRole;
import com.example.demo.model.User;
import com.example.demo.permissions.Permission;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.PermissionService;
import com.example.demo.service.RoleService;

/**
 * REST API endpoints for role management and permissions
 * Provides comprehensive role administration capabilities
 */
@RestController
@RequestMapping("/api/roles")
public class RoleController {
    private static final Logger log = LoggerFactory.getLogger(RoleController.class);

    @Autowired
    private RoleService roleService;

    @Autowired
    private PermissionService permissionService;

    @Autowired
    private UserRepository userRepository;

    /**
     * Get all available roles with their permissions
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllRoles() {
        try {
            Map<String, Object> response = new HashMap<>();

            for (UserRole role : UserRole.getRolesByHierarchy()) {
                Map<String, Object> roleInfo = new HashMap<>();
                roleInfo.put("name", role.name());
                roleInfo.put("displayName", role.getDisplayName());
                roleInfo.put("level", role.getLevel());
                roleInfo.put("color", role.getColor());
                roleInfo.put("icon", role.getIcon());
                roleInfo.put("description", role.getDescription());
                roleInfo.put("permissions", role.getPermissionNames());
                roleInfo.put("requiresManualProgression", role.requiresManualProgression());

                // Add progression criteria
                UserRole.RoleProgressionCriteria criteria = role.getProgressionCriteria();
                Map<String, Object> progressionInfo = new HashMap<>();
                progressionInfo.put("minDaysInRole", criteria.getMinDaysInRole());
                progressionInfo.put("minMessagesSent", criteria.getMinMessagesSent());
                progressionInfo.put("isAutomatic", criteria.isAutomatic());
                progressionInfo.put("approvalLevel", criteria.getApprovalLevel());
                roleInfo.put("progressionCriteria", progressionInfo);

                response.put(role.name(), roleInfo);
            }

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error fetching roles", e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Failed to fetch roles: " + e.getMessage()));
        }
    }

    /**
     * Get user's current role information
     */
    @GetMapping("/users/{username}")
    public ResponseEntity<Map<String, Object>> getUserRole(@PathVariable String username) {
        try {
            UserRole role = permissionService.getUserRole(username);
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found: " + username));

            Map<String, Object> response = new HashMap<>();
            response.put("username", username);
            response.put("role", role.name());
            response.put("roleDisplayName", role.getDisplayName());
            response.put("level", role.getLevel());
            response.put("permissions", role.getPermissionNames());
            response.put("roleAssignedAt", user.getRoleAssignedAt());
            response.put("roleChangedBy", user.getRoleChangedBy());
            response.put("messageCount", user.getMessageCount());
            response.put("accountAgeDays", user.getAccountAgeInDays());
            response.put("daysInCurrentRole", user.getDaysInCurrentRole());
            response.put("eligibleForProgression", user.isEligibleForProgression());

            UserRole nextRole = role.getNextRole();
            if (nextRole != role) {
                Map<String, Object> nextRoleInfo = new HashMap<>();
                nextRoleInfo.put("name", nextRole.name());
                nextRoleInfo.put("displayName", nextRole.getDisplayName());
                nextRoleInfo.put("requiresManualApproval", nextRole.requiresManualProgression());
                response.put("nextRole", nextRoleInfo);
            }

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error fetching user role for {}", username, e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Failed to fetch user role: " + e.getMessage()));
        }
    }

    /**
     * Assign a new role to a user (admin only)
     */
    @PostMapping("/users/{username}/assign")
    public ResponseEntity<Map<String, Object>> assignRole(
            @PathVariable String username,
            @RequestBody RoleAssignmentRequest request,
            Principal principal) {

        String assignedBy = principal.getName();

        try {
            // Validate that the assigner has permission to assign this role
            if (!permissionService.canAssignRole(assignedBy, request.getRole())) {
                return ResponseEntity.status(403)
                        .body(Map.of("error", "You don't have permission to assign role: " + request.getRole()));
            }

            // Perform the role assignment
            User updatedUser = roleService.promoteUser(username, request.getRole(), assignedBy);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Role assigned successfully");
            response.put("username", username);
            response.put("previousRole", request.getPreviousRole());
            response.put("newRole", updatedUser.getUserRole().name());
            response.put("assignedBy", assignedBy);
            response.put("assignedAt", updatedUser.getRoleAssignedAt());

            return ResponseEntity.ok(response);

        } catch (RoleService.RoleException e) {
            log.warn("Role assignment failed for {}: {}", username, e.getMessage());
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));

        } catch (Exception e) {
            log.error("Error assigning role to user {}", username, e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Failed to assign role: " + e.getMessage()));
        }
    }

    /**
     * Promote user to next role level
     */
    @PostMapping("/users/{username}/promote")
    public ResponseEntity<Map<String, Object>> promoteUser(
            @PathVariable String username,
            @RequestBody PromotionRequest request,
            Principal principal) {

        String promotedBy = principal.getName();

        try {
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found: " + username));
            UserRole nextRole = user.getUserRole().getNextRole();

            if (nextRole == user.getUserRole()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "User is already at the highest role level"));
            }

            // Validate permission
            if (!permissionService.canAssignRole(promotedBy, nextRole)) {
                return ResponseEntity.status(403)
                        .body(Map.of("error", "You don't have permission to promote to: " + nextRole.getDisplayName()));
            }

            // Perform promotion
            User updatedUser = roleService.promoteUser(username, nextRole, promotedBy);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "User promoted successfully");
            response.put("username", username);
            response.put("previousRole", user.getUserRole().name());
            response.put("newRole", updatedUser.getUserRole().name());
            response.put("promotedBy", promotedBy);
            response.put("promotedAt", updatedUser.getRoleAssignedAt());

            return ResponseEntity.ok(response);

        } catch (RoleService.RoleException e) {
            log.warn("Promotion failed for {}: {}", username, e.getMessage());
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));

        } catch (Exception e) {
            log.error("Error promoting user {}", username, e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Failed to promote user: " + e.getMessage()));
        }
    }

    /**
     * Demote user to lower role
     */
    @PostMapping("/users/{username}/demote")
    public ResponseEntity<Map<String, Object>> demoteUser(
            @PathVariable String username,
            @RequestBody DemotionRequest request,
            Principal principal) {

        String demotedBy = principal.getName();

        try {
            // Validate permission
            if (!permissionService.canAssignRole(demotedBy, request.getTargetRole())) {
                return ResponseEntity.status(403)
                        .body(Map.of("error", "You don't have permission to assign role: " + request.getTargetRole()));
            }

            // Perform demotion
            User updatedUser = roleService.demoteUser(username, request.getTargetRole(), demotedBy);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "User demoted successfully");
            response.put("username", username);
            response.put("previousRole", request.getPreviousRole());
            response.put("newRole", updatedUser.getUserRole().name());
            response.put("demotedBy", demotedBy);
            response.put("demotedAt", updatedUser.getRoleAssignedAt());

            return ResponseEntity.ok(response);

        } catch (RoleService.RoleException e) {
            log.warn("Demotion failed for {}: {}", username, e.getMessage());
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));

        } catch (Exception e) {
            log.error("Error demoting user {}", username, e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Failed to demote user: " + e.getMessage()));
        }
    }

    /**
     * Get users with specific role
     */
    @GetMapping("/{role}/users")
    public ResponseEntity<Map<String, Object>> getUsersByRole(@PathVariable String role) {
        try {
            UserRole userRole = UserRole.fromString(role);
            List<User> users = roleService.getUsersByRole(userRole);

            Map<String, Object> response = new HashMap<>();
            response.put("role", role);
            response.put("roleDisplayName", userRole.getDisplayName());
            response.put("userCount", users.size());

            List<Map<String, Object>> userList = users.stream()
                    .map(this::createUserInfoMap)
                    .toList();
            response.put("users", userList);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error fetching users for role {}", role, e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Failed to fetch users: " + e.getMessage()));
        }
    }

    /**
     * Check if user has specific permission
     */
    @PostMapping("/check-permission")
    public ResponseEntity<Map<String, Object>> checkPermission(@RequestBody PermissionCheckRequest request) {
        try {
            boolean hasPermission = permissionService.hasPermission(request.getUsername(),
                    Permission.fromName(request.getPermission()));

            Map<String, Object> response = new HashMap<>();
            response.put("username", request.getUsername());
            response.put("permission", request.getPermission());
            response.put("hasPermission", hasPermission);
            response.put("userRole", permissionService.getUserRole(request.getUsername()).name());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error checking permission for user {}", request.getUsername(), e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Failed to check permission: " + e.getMessage()));
        }
    }

    /**
     * Get role statistics
     */
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getRoleStatistics(Principal principal) {
        try {
            // Check if user has permission to view stats
            if (!permissionService.hasPermission(principal.getName(), Permission.VIEW_SYSTEM_STATS)) {
                return ResponseEntity.status(403)
                        .body(Map.of("error", "You don't have permission to view role statistics"));
            }

            RoleService.RoleStatistics stats = roleService.getRoleStatistics();

            Map<String, Object> response = new HashMap<>();
            response.put("totalUsers", stats.getTotalUsers());
            response.put("roleDistribution", stats.getAllCounts());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error fetching role statistics", e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Failed to fetch statistics: " + e.getMessage()));
        }
    }

    /**
     * Get users eligible for promotion
     */
    @GetMapping("/eligible-for-promotion")
    public ResponseEntity<Map<String, Object>> getEligibleUsers(
            @RequestParam(required = false) String role,
            Principal principal) {

        try {
            // Check if user has permission to view this information
            if (!permissionService.hasPermission(principal.getName(), Permission.MANAGE_ROLES)) {
                return ResponseEntity.status(403)
                        .body(Map.of("error", "You don't have permission to view eligible users"));
            }

            UserRole targetRole = role != null ? UserRole.fromString(role) : UserRole.NEW_MEMBER;
            List<User> eligibleUsers = roleService.getUsersEligibleForPromotion(targetRole);

            Map<String, Object> response = new HashMap<>();
            response.put("targetRole", targetRole.name());
            response.put("eligibleCount", eligibleUsers.size());

            List<Map<String, Object>> userList = eligibleUsers.stream()
                    .map(this::createUserInfoMap)
                    .toList();
            response.put("eligibleUsers", userList);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error fetching eligible users", e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Failed to fetch eligible users: " + e.getMessage()));
        }
    }

    /**
     * Process automatic role progression (admin only)
     */
    @PostMapping("/process-automatic-progression")
    public ResponseEntity<Map<String, Object>> processAutomaticProgression(Principal principal) {
        try {
            // Only Super Admin can trigger this
            if (!permissionService.isSuperAdmin(principal.getName())) {
                return ResponseEntity.status(403)
                        .body(Map.of("error", "Only Super Admin can process automatic progression"));
            }

            int processedCount = roleService.processAutomaticProgressions();

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Automatic progression processed successfully");
            response.put("processedCount", processedCount);
            response.put("processedBy", principal.getName());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error processing automatic progression", e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Failed to process automatic progression: " + e.getMessage()));
        }
    }

    // === HELPER METHODS ===

    private Map<String, Object> createUserInfoMap(User user) {
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("username", user.getUsername());
        userInfo.put("email", user.getEmail());
        userInfo.put("role", user.getUserRole().name());
        userInfo.put("roleDisplayName", user.getUserRole().getDisplayName());
        userInfo.put("messageCount", user.getMessageCount());
        userInfo.put("accountAgeDays", user.getAccountAgeInDays());
        userInfo.put("daysInCurrentRole", user.getDaysInCurrentRole());
        userInfo.put("roleAssignedAt", user.getRoleAssignedAt());
        userInfo.put("roleChangedBy", user.getRoleChangedBy());
        userInfo.put("lastActivityAt", user.getLastActivityAt());
        userInfo.put("isOnline", user.isOnline());
        userInfo.put("eligibleForProgression", user.isEligibleForProgression());
        return userInfo;
    }

    // === REQUEST DTOs ===

    public static class RoleAssignmentRequest {
        private UserRole role;
        private UserRole previousRole;

        public UserRole getRole() { return role; }
        public void setRole(UserRole role) { this.role = role; }
        public UserRole getPreviousRole() { return previousRole; }
        public void setPreviousRole(UserRole previousRole) { this.previousRole = previousRole; }
    }

    public static class PromotionRequest {
        // Empty for now - could add reason or notes in the future
    }

    public static class DemotionRequest {
        private UserRole targetRole;
        private UserRole previousRole;
        private String reason;

        public UserRole getTargetRole() { return targetRole; }
        public void setTargetRole(UserRole targetRole) { this.targetRole = targetRole; }
        public UserRole getPreviousRole() { return previousRole; }
        public void setPreviousRole(UserRole previousRole) { this.previousRole = previousRole; }
        public String getReason() { return reason; }
        public void setReason(String reason) { this.reason = reason; }
    }

    public static class PermissionCheckRequest {
        private String username;
        private String permission;

        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getPermission() { return permission; }
        public void setPermission(String permission) { this.permission = permission; }
    }
}