package com.example.demo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
// Removed incorrect import for MethodSecurityExpressionHandler
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import com.example.demo.enums.Permission;
import com.example.demo.service.PermissionService;

/**
 * Method-level security configuration for role-based permissions
 * Enables @PreAuthorize annotations with custom permission checking
 */
@Configuration
@EnableMethodSecurity(prePostEnabled = true, securedEnabled = true, jsr250Enabled = true)
public class RoleBasedMethodSecurity {

    @Bean
    // Removed incorrect annotation usage
    public MethodSecurityExpressionHandler createExpressionHandler(PermissionService permissionService) {
        DefaultMethodSecurityExpressionHandler handler = new DefaultMethodSecurityExpressionHandler();
        handler.setDefaultRolePrefix(""); // No prefix for role names
        return handler;
    }

    /**
     * Custom permission evaluator for method-level security
     */
    public static class CustomPermissionEvaluator {
        private final PermissionService permissionService;

        public CustomPermissionEvaluator(PermissionService permissionService) {
            this.permissionService = permissionService;
        }

        /**
         * Check if current user has a specific permission
         */
        public boolean hasPermission(String permission) {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || !auth.isAuthenticated()) {
                return false;
            }

            String username = auth.getName();
            return permissionService.hasPermission(username, Permission.fromName(permission));
        }

        /**
         * Check if current user has any of the specified permissions
         */
        public boolean hasAnyPermission(String... permissions) {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || !auth.isAuthenticated()) {
                return false;
            }

            String username = auth.getName();
            for (String permission : permissions) {
                if (permissionService.hasPermission(username, Permission.fromName(permission))) {
                    return true;
                }
            }
            return false;
        }

        /**
         * Check if current user is moderator or higher
         */
        public boolean isModeratorOrHigher() {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || !auth.isAuthenticated()) {
                return false;
            }

            return permissionService.isModeratorOrHigher(auth.getName());
        }

        /**
         * Check if current user is admin or higher
         */
        public boolean isAdminOrHigher() {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || !auth.isAuthenticated()) {
                return false;
            }

            return permissionService.isAdminOrHigher(auth.getName());
        }

        /**
         * Check if current user is super admin
         */
        public boolean isSuperAdmin() {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || !auth.isAuthenticated()) {
                return false;
            }

            return permissionService.isSuperAdmin(auth.getName());
        }

        /**
         * Check if user can moderate another user
         */
        public boolean canModerateUser(String targetUsername) {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || !auth.isAuthenticated()) {
                return false;
            }

            String moderatorUsername = auth.getName();
            return permissionService.canMuteUser(moderatorUsername, targetUsername);
        }

        /**
         * Check if user can delete another user's message
         */
        public boolean canDeleteMessage(String messageAuthorUsername) {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || !auth.isAuthenticated()) {
                return false;
            }

            String moderatorUsername = auth.getName();
            return permissionService.canDeleteMessage(moderatorUsername, messageAuthorUsername);
        }

        /**
         * Check if user can assign specific role
         */
        public boolean canAssignRole(String roleName) {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || !auth.isAuthenticated()) {
                return false;
            }

            try {
                com.example.demo.enums.UserRole targetRole = com.example.demo.enums.UserRole.valueOf(roleName);
                return permissionService.canAssignRole(auth.getName(), targetRole);
            } catch (IllegalArgumentException e) {
                return false;
            }
        }

        /**
         * Check if user can access admin panel
         */
        public boolean canAccessAdminPanel() {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || !auth.isAuthenticated()) {
                return false;
            }

            return permissionService.canAccessAdminPanel(auth.getName());
        }

        /**
         * Check if user can view system statistics
         */
        public boolean canViewSystemStats() {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || !auth.isAuthenticated()) {
                return false;
            }

            return permissionService.canViewSystemStats(auth.getName());
        }

        /**
         * Check if user can view audit logs
         */
        public boolean canViewAuditLogs() {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || !auth.isAuthenticated()) {
                return false;
            }

            return permissionService.canViewAuditLogs(auth.getName());
        }

        /**
         * Check if user can perform system configuration
         */
        public boolean canConfigureSystem() {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || !auth.isAuthenticated()) {
                return false;
            }

            return permissionService.canConfigureSystem(auth.getName());
        }

        /**
         * Check if user can access database
         */
        public boolean canAccessDatabase() {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || !auth.isAuthenticated()) {
                return false;
            }

            return permissionService.canAccessDatabase(auth.getName());
        }

        /**
         * Check if user is the resource owner or has admin privileges
         */
        public boolean isOwnerOrAdmin(String resourceUsername) {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || !auth.isAuthenticated()) {
                return false;
            }

            String currentUsername = auth.getName();

            // User can always access their own resources
            if (currentUsername.equals(resourceUsername)) {
                return true;
            }

            // Check if user has admin privileges
            return permissionService.isAdminOrHigher(currentUsername);
        }

        /**
         * Check if user is the resource owner or has moderator privileges
         */
        public boolean isOwnerOrModerator(String resourceUsername) {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || !auth.isAuthenticated()) {
                return false;
            }

            String currentUsername = auth.getName();

            // User can always access their own resources
            if (currentUsername.equals(resourceUsername)) {
                return true;
            }

            // Check if user has moderator privileges
            return permissionService.isModeratorOrHigher(currentUsername);
        }
    }
}