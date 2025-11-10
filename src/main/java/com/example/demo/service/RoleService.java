package com.example.demo.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.enums.UserRole;
import com.example.demo.model.User;
import com.example.demo.permissions.Permission;
import com.example.demo.repository.UserRepository;

/**
 * Core role management service for handling role assignments,
 * progressions, and administrative role operations.
 */
@Service
@Transactional
public class RoleService {
    private static final Logger log = LoggerFactory.getLogger(RoleService.class);

    @Autowired
    private UserRepository userRepository;

    /**
     * Promote a user to a specific role with validation and audit trail
     */
    @Transactional
    public User promoteUser(String username, UserRole targetRole, String assignedBy) throws RoleException {
        log.info("Attempting to promote user {} to role {} by {}", username, targetRole, assignedBy);

        User user = findUserByUsername(username);
        User assigner = findUserByUsername(assignedBy);

        // Validate promotion
        validatePromotion(user, targetRole, assigner);

        // Get previous role for audit
        UserRole previousRole = user.getUserRole();

        // Update user role
        user.setUserRole(targetRole);
        user.setRoleAssignedAt(LocalDateTime.now());
        user.setRoleChangedBy(assignedBy);

        // Save and return updated user
        User savedUser = userRepository.save(user);

        // Log the promotion
        logRoleChange(savedUser, previousRole, targetRole, assignedBy, "PROMOTION");

        log.info("Successfully promoted user {} from {} to {}", username, previousRole, targetRole);
        return savedUser;
    }

    /**
     * Demote a user to a lower role with validation and audit trail
     */
    @Transactional
    public User demoteUser(String username, UserRole targetRole, String assignedBy) throws RoleException {
        log.info("Attempting to demote user {} to role {} by {}", username, targetRole, assignedBy);

        User user = findUserByUsername(username);
        User assigner = findUserByUsername(assignedBy);

        // Validate demotion
        validateDemotion(user, targetRole, assigner);

        // Get previous role for audit
        UserRole previousRole = user.getUserRole();

        // Update user role
        user.setUserRole(targetRole);
        user.setRoleAssignedAt(LocalDateTime.now());
        user.setRoleChangedBy(assignedBy);

        // Save and return updated user
        User savedUser = userRepository.save(user);

        // Log the demotion
        logRoleChange(savedUser, previousRole, targetRole, assignedBy, "DEMOTION");

        log.info("Successfully demoted user {} from {} to {}", username, previousRole, targetRole);
        return savedUser;
    }

    /**
     * Check and process automatic role progression for a user
     */
    @Transactional
    public Optional<User> checkAutomaticProgression(String username) {
        log.debug("Checking automatic progression for user {}", username);

        User user = findUserByUsername(username);

        // Update last activity
        user.updateLastActivity();

        // Check if user is eligible for progression
        if (!user.isEligibleForProgression()) {
            log.debug("User {} is not eligible for automatic progression", username);
            return Optional.empty();
        }

        UserRole nextRole = user.getUserRole().getNextRole();

        // Check if next role requires manual approval
        if (user.getUserRole().requiresManualProgression()) {
            log.info("User {} eligible for progression to {} but requires manual approval",
                    username, nextRole);
            // Could trigger notification to admins here
            return Optional.empty();
        }

        // Get previous role for audit
        UserRole previousRole = user.getUserRole();

        // Perform automatic promotion
        user.setUserRole(nextRole);
        user.setRoleAssignedAt(LocalDateTime.now());
        user.setRoleChangedBy("SYSTEM_AUTO");

        // Save and return updated user
        User savedUser = userRepository.save(user);

        // Log the automatic promotion
        logRoleChange(savedUser, previousRole, nextRole, "SYSTEM_AUTO", "AUTO_PROMOTION");

        log.info("Automatically promoted user {} from {} to {}", username, previousRole, nextRole);
        return Optional.of(savedUser);
    }

    /**
     * Batch process automatic role progression for all eligible users
     */
    @Transactional
    public int processAutomaticProgressions() {
        log.info("Starting batch automatic role progression check");

        int processedCount = 0;
        List<User> allUsers = userRepository.findAll();

        for (User user : allUsers) {
            if (user.isEligibleForProgression()) {
                try {
                    Optional<User> result = checkAutomaticProgression(user.getUsername());
                    if (result.isPresent()) {
                        processedCount++;
                    }
                } catch (Exception e) {
                    log.error("Error processing automatic progression for user {}: {}",
                             user.getUsername(), e.getMessage());
                }
            }
        }

        log.info("Completed batch automatic role progression. {} users promoted.", processedCount);
        return processedCount;
    }

    /**
     * Centralized permission checking
     */
    @Transactional(readOnly = true)
    public boolean hasPermission(String username, Permission permission) {
        try {
            User user = findUserByUsername(username);
            return user.hasPermission(permission);
        } catch (Exception e) {
            log.warn("Error checking permission for user {}: {}", username, e.getMessage());
            return false;
        }
    }

    /**
     * Get all users with a specific role
     */
    @Transactional(readOnly = true)
    public List<User> getUsersByRole(UserRole role) {
        return userRepository.findByUserRole(role);
    }

    /**
     * Get users eligible for promotion to next role level
     */
    @Transactional(readOnly = true)
    public List<User> getUsersEligibleForPromotion(UserRole currentRole) {
        return userRepository.findUsersEligibleForPromotion(currentRole);
    }

    /**
     * Get role statistics
     */
    @Transactional(readOnly = true)
    public RoleStatistics getRoleStatistics() {
        RoleStatistics stats = new RoleStatistics();

        for (UserRole role : UserRole.values()) {
            int count = userRepository.countByUserRole(role);
            stats.addRoleCount(role, count);
        }

        return stats;
    }

    /**
     * Initialize role system for existing users (migration helper)
     */
    @Transactional
    public int initializeRolesForExistingUsers() {
        log.info("Initializing role system for existing users");

        int updatedCount = 0;
        List<User> users = userRepository.findAll();

        for (User user : users) {
            // Skip if user already has a role assigned
            if (user.getUserRole() != UserRole.NEW_MEMBER && user.getRoleAssignedAt() != null) {
                continue;
            }

            // Migrate from legacy boolean fields
            UserRole assignedRole = determineRoleFromLegacyFields(user);

            if (assignedRole != user.getUserRole()) {
                user.setUserRole(assignedRole);
                user.setRoleAssignedAt(LocalDateTime.now());
                user.setRoleChangedBy("SYSTEM_MIGRATION");

                // Set account creation date if not set
                if (user.getAccountCreatedAt() == null) {
                    user.setAccountCreatedAt(LocalDateTime.now());
                }

                userRepository.save(user);
                updatedCount++;

                log.debug("Migrated user {} from legacy fields to role {}",
                         user.getUsername(), assignedRole);
            }
        }

        log.info("Role system initialization completed. {} users updated.", updatedCount);
        return updatedCount;
    }

    // === PRIVATE HELPER METHODS ===

    private User findUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RoleException("User not found: " + username));
    }

    private void validatePromotion(User user, UserRole targetRole, User assigner) throws RoleException {
        // Validate target role
        if (targetRole == null) {
            throw new RoleException("Target role cannot be null");
        }

        // Check if assigner can promote to target role
        if (!assigner.getUserRole().canPromoteTo(targetRole)) {
            throw new RoleException(String.format(
                "User %s with role %s cannot promote users to role %s",
                assigner.getUsername(), assigner.getUserRole(), targetRole));
        }

        // Check if user already has target or higher role
        if (user.getUserRole().isHigherThanOrEqual(targetRole)) {
            throw new RoleException(String.format(
                "User %s already has role %s or higher", user.getUsername(), targetRole));
        }

        // Additional validation for manual progression requirements
        if (targetRole.requiresManualProgression()) {
            UserRole.RoleProgressionCriteria criteria = user.getUserRole().getProgressionCriteria();

            if (criteria.requiresModeratorApproval() && !assigner.canModerate()) {
                throw new RoleException("Promotion to " + targetRole + " requires moderator approval");
            }

            if (criteria.requiresAdminApproval() && !assigner.canAdministrate()) {
                throw new RoleException("Promotion to " + targetRole + " requires admin approval");
            }

            if (criteria.requiresSuperAdminApproval() && assigner.getUserRole() != UserRole.SUPER_ADMIN) {
                throw new RoleException("Promotion to " + targetRole + " requires super admin approval");
            }
        }
    }

    private void validateDemotion(User user, UserRole targetRole, User assigner) throws RoleException {
        // Validate target role
        if (targetRole == null) {
            throw new RoleException("Target role cannot be null");
        }

        // Only Super Admin can demote Admins, and Admins can demote Moderators, etc.
        if (assigner.getUserRole().getLevel() <= user.getUserRole().getLevel()) {
            throw new RoleException(String.format(
                "User %s with role %s cannot demote user %s with role %s",
                assigner.getUsername(), assigner.getUserRole(),
                user.getUsername(), user.getUserRole()));
        }

        // Check if target role is not higher than user's current role
        if (targetRole.isHigherThanOrEqual(user.getUserRole())) {
            throw new RoleException("Cannot demote to a role that is equal or higher than current role");
        }
    }

    private UserRole determineRoleFromLegacyFields(User user) {
        if (user.getIsSuperAdmin()) {
            return UserRole.SUPER_ADMIN;
        } else if (user.getIsAdmin()) {
            return UserRole.ADMIN;
        } else if (user.getIsMuted()) {
            return UserRole.NEW_MEMBER; // Keep muted users at lowest level
        } else if (user.getIsBanned()) {
            return UserRole.NEW_MEMBER; // Keep banned users at lowest level
        } else {
            // For regular users, determine based on account age and message count
            long accountAgeDays = user.getAccountAgeInDays();
            int messageCount = user.getMessageCount();

            if (accountAgeDays >= 30 && messageCount >= 100) {
                return UserRole.VIP;
            } else if (accountAgeDays >= 7 && messageCount >= 10) {
                return UserRole.MEMBER;
            } else {
                return UserRole.NEW_MEMBER;
            }
        }
    }

    private void logRoleChange(User user, UserRole previousRole, UserRole newRole,
                              String changedBy, String changeType) {
        log.info("ROLE_CHANGE: User={}, Previous={}, New={}, ChangedBy={}, Type={}, Timestamp={}",
                user.getUsername(), previousRole, newRole, changedBy, changeType, LocalDateTime.now());

        // In a production system, you would also store this in an audit log table
        // auditLogService.logRoleChange(user, previousRole, newRole, changedBy, changeType);
    }

    /**
     * Custom exception for role-related operations
     */
    public static class RoleException extends Exception {
        public RoleException(String message) {
            super(message);
        }

        public RoleException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    /**
     * Data class for role statistics
     */
    public static class RoleStatistics {
        private final java.util.Map<UserRole, Integer> roleCounts = new java.util.HashMap<>();

        public void addRoleCount(UserRole role, int count) {
            roleCounts.put(role, count);
        }

        public int getCount(UserRole role) {
            return roleCounts.getOrDefault(role, 0);
        }

        public java.util.Map<UserRole, Integer> getAllCounts() {
            return new java.util.HashMap<>(roleCounts);
        }

        public int getTotalUsers() {
            return roleCounts.values().stream().mapToInt(Integer::intValue).sum();
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder("RoleStatistics{totalUsers=").append(getTotalUsers());
            roleCounts.forEach((role, count) ->
                sb.append(", ").append(role).append("=").append(count));
            sb.append("}");
            return sb.toString();
        }
    }
}