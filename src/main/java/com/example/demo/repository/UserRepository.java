package com.example.demo.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.demo.enums.UserRole;
import com.example.demo.model.User;

public interface UserRepository extends JpaRepository<User, Long> {
    // === BASIC USER QUERIES ===

    List<User> findByUsernameIn(Set<String> usernames);
    List<User> findByUsernameIn(List<String> usernames); // For performance optimization
    User findByUsername(String username);
    User findByEmail(String email);

    // === ROLE-SPECIFIC QUERIES ===

    /**
     * Find users by their role
     */
    List<User> findByUserRole(UserRole role);

    /**
     * Count users by role
     */
    long countByUserRole(UserRole role);

    /**
     * Find users with role level >= specified level
     */
    @Query("SELECT u FROM User u WHERE u.userRole.level >= :minLevel ORDER BY u.userRole.level DESC, u.username ASC")
    List<User> findByUserRoleLevelGreaterThanEqual(@Param("minLevel") int minLevel);

    /**
     * Find users eligible for promotion from a specific role
     */
    @Query("SELECT u FROM User u WHERE u.userRole = :currentRole " +
           "AND (" +
           "  (u.userRole = 'NEW_MEMBER' AND u.accountCreatedAt <= :cutoffDate AND u.messageCount >= 10) OR " +
           "  (u.userRole = 'MEMBER' AND u.roleAssignedAt <= :cutoffDate AND u.messageCount >= 100) OR " +
           "  (u.userRole IN ('VIP', 'MODERATOR', 'ADMIN'))" +
           ")")
    List<User> findUsersEligibleForPromotion(@Param("currentRole") UserRole currentRole,
                                           @Param("cutoffDate") LocalDateTime cutoffDate);

    /**
     * Find users eligible for automatic progression (simplified version)
     */
    @Query("SELECT u FROM User u WHERE u.lastRoleProgressionCheck IS NULL OR u.lastRoleProgressionCheck < :since")
    List<User> findUsersForProgressionCheck(@Param("since") LocalDateTime since);

    /**
     * Find recently active users within a time window
     */
    @Query("SELECT u FROM User u WHERE u.lastActivityAt >= :since ORDER BY u.lastActivityAt DESC")
    List<User> findRecentlyActiveUsers(@Param("since") LocalDateTime since);

    /**
     * Find users by message count range
     */
    @Query("SELECT u FROM User u WHERE u.messageCount BETWEEN :minMessages AND :maxMessages ORDER BY u.messageCount DESC")
    List<User> findByMessageCountRange(@Param("minMessages") int minMessages,
                                      @Param("maxMessages") int maxMessages);

    /**
     * Find users who haven't been active for a specified period
     */
    @Query("SELECT u FROM User u WHERE (u.lastActivityAt IS NULL OR u.lastActivityAt < :before) ORDER BY u.lastActivityAt ASC")
    List<User> findInactiveUsers(@Param("before") LocalDateTime before);

    /**
     * Update user role with audit trail
     */
    @Query("UPDATE User u SET u.userRole = :newRole, u.roleAssignedAt = :assignedAt, u.roleChangedBy = :changedBy WHERE u.id = :userId")
    int updateUserRole(@Param("userId") Long userId,
                       @Param("newRole") UserRole newRole,
                       @Param("assignedAt") LocalDateTime assignedAt,
                       @Param("changedBy") String changedBy);

    /**
     * Increment message count for a user
     */
    @Query("UPDATE User u SET u.messageCount = u.messageCount + 1, u.lastActivityAt = :activityTime WHERE u.id = :userId")
    int incrementMessageCount(@Param("userId") Long userId, @Param("activityTime") LocalDateTime activityTime);

    /**
     * Update last activity timestamp
     */
    @Query("UPDATE User u SET u.lastActivityAt = :activityTime WHERE u.username = :username")
    int updateLastActivity(@Param("username") String username, @Param("activityTime") LocalDateTime activityTime);

    /**
     * Get role distribution statistics
     */
    @Query("SELECT u.userRole, COUNT(u) FROM User u GROUP BY u.userRole ORDER BY u.userRole.level")
    List<Object[]> getRoleDistributionStatistics();

    /**
     * Get activity statistics for a date range
     */
    @Query("SELECT DATE(u.lastActivityAt), COUNT(u) FROM User u WHERE u.lastActivityAt BETWEEN :start AND :end GROUP BY DATE(u.lastActivityAt) ORDER BY DATE(u.lastActivityAt)")
    List<Object[]> getActivityStatistics(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    /**
     * Find users by role progression criteria
     */
    @Query("SELECT u FROM User u WHERE u.userRole = :role " +
           "AND u.messageCount >= :minMessages " +
           "AND (:checkAge = false OR u.roleAssignedAt <= :cutoffDate)")
    List<User> findUsersByProgressionCriteria(@Param("role") UserRole role,
                                            @Param("minMessages") int minMessages,
                                            @Param("checkAge") boolean checkAge,
                                            @Param("cutoffDate") LocalDateTime cutoffDate);

    // === LEGACY ADMIN QUERIES (kept for backward compatibility) ===

    // Admin queries
    long countByIsAdminTrue();
    long countByIsMutedTrue();
    long countByIsBannedTrue();
    List<User> findByIsAdminTrue();
    List<User> findByIsSuperAdminTrue();

    // Additional admin methods
    List<User> findByIsBannedTrue();
    List<User> findByIsMutedTrue();
    List<User> findByUsernameContainingIgnoreCaseOrEmailContainingIgnoreCase(String username, String email);

    // === COMPLEX SEARCH QUERIES ===

    /**
     * Search users by multiple criteria
     */
    @Query("SELECT u FROM User u WHERE " +
           "(:username IS NULL OR LOWER(u.username) LIKE LOWER(CONCAT('%', :username, '%'))) AND " +
           "(:email IS NULL OR LOWER(u.email) LIKE LOWER(CONCAT('%', :email, '%'))) AND " +
           "(:role IS NULL OR u.userRole = :role) AND " +
           "(:minMessages IS NULL OR u.messageCount >= :minMessages) AND " +
           "(:maxMessages IS NULL OR u.messageCount <= :maxMessages) " +
           "ORDER BY u.username ASC")
    List<User> searchUsers(@Param("username") String username,
                          @Param("email") String email,
                          @Param("role") UserRole role,
                          @Param("minMessages") Integer minMessages,
                          @Param("maxMessages") Integer maxMessages);

    /**
     * Find users with specific permission
     */
    @Query("SELECT u FROM User u WHERE u.userRole.level >= :requiredLevel")
    List<User> findUsersWithPermissionLevel(@Param("requiredLevel") int requiredLevel);

    /**
     * Get users sorted by message count (top contributors)
     */
    @Query("SELECT u FROM User u ORDER BY u.messageCount DESC")
    List<User> findTopContributors();

    /**
     * Get users sorted by account creation date (newest users)
     */
    @Query("SELECT u FROM User u ORDER BY u.accountCreatedAt DESC")
    List<User> findNewestUsers();
}
