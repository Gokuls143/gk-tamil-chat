-- Migration V2: Add comprehensive user role system
-- This migration adds the new role-based permission system while maintaining backward compatibility

-- Add new role-related columns to users table
ALTER TABLE users
ADD COLUMN user_role VARCHAR(20) DEFAULT 'NEW_MEMBER',
ADD COLUMN role_assigned_at TIMESTAMP NULL,
ADD COLUMN last_activity_at TIMESTAMP NULL,
ADD COLUMN role_changed_by VARCHAR(255) NULL,
ADD COLUMN message_count INT DEFAULT 0,
ADD COLUMN account_created_at TIMESTAMP NULL,
ADD COLUMN last_role_progression_check TIMESTAMP NULL;

-- Add indexes for role-based queries
CREATE INDEX idx_users_role ON users(user_role);
CREATE INDEX idx_users_activity ON users(last_activity_at);
CREATE INDEX idx_users_message_count ON users(message_count);
CREATE INDEX idx_users_account_created ON users(account_created_at);
CREATE INDEX idx_users_role_assigned_at ON users(role_assigned_at);

-- Populate initial roles from existing boolean fields
UPDATE users SET
    user_role = CASE
        WHEN is_super_admin = true THEN 'SUPER_ADMIN'
        WHEN is_admin = true THEN 'ADMIN'
        WHEN is_muted = true OR is_banned = true THEN 'NEW_MEMBER'
        ELSE 'MEMBER'  -- Default regular users to Member role
    END,
    role_assigned_at = COALESCE(
        CASE
            WHEN is_super_admin = true OR is_admin = true THEN account_created_at
            ELSE account_created_at
        END,
        NOW()
    ),
    account_created_at = COALESCE(account_created_at, NOW()),
    message_count = COALESCE(message_count, 0),
    role_changed_by = 'SYSTEM_MIGRATION'
WHERE user_role IS NULL OR user_role = 'NEW_MEMBER';

-- Set account creation dates for existing users if not set
UPDATE users SET account_created_at = NOW()
WHERE account_created_at IS NULL;

-- Initialize role progression check timestamp
UPDATE users SET last_role_progression_check = NOW()
WHERE last_role_progression_check IS NULL;

-- Create audit log table for role changes (for future implementation)
CREATE TABLE IF NOT EXISTS role_audit_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    username VARCHAR(255) NOT NULL,
    previous_role VARCHAR(20) NULL,
    new_role VARCHAR(20) NOT NULL,
    changed_by VARCHAR(255) NOT NULL,
    change_type VARCHAR(20) NOT NULL, -- PROMOTION, DEMOTION, AUTO_PROMOTION, SYSTEM_MIGRATION
    change_reason TEXT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_role_audit_user_id (user_id),
    INDEX idx_role_audit_username (username),
    INDEX idx_role_audit_created_at (created_at),
    INDEX idx_role_audit_change_type (change_type)
);

-- Create role progression queue table (for future batch processing)
CREATE TABLE IF NOT EXISTS role_progression_queue (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    username VARCHAR(255) NOT NULL,
    current_role VARCHAR(20) NOT NULL,
    target_role VARCHAR(20) NOT NULL,
    eligibility_reason TEXT NULL,
    status VARCHAR(20) DEFAULT 'PENDING', -- PENDING, PROCESSED, FAILED, SKIPPED
    processed_at TIMESTAMP NULL,
    error_message TEXT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_progression_queue_user_id (user_id),
    INDEX idx_progression_queue_status (status),
    INDEX idx_progression_queue_created_at (created_at),
    INDEX idx_progression_queue_current_role (current_role)
);

-- Insert initial audit entries for migrated users
INSERT INTO role_audit_log (user_id, username, previous_role, new_role, changed_by, change_type, change_reason)
SELECT
    id,
    username,
    'LEGACY_SYSTEM',
    user_role,
    'SYSTEM_MIGRATION',
    'SYSTEM_MIGRATION',
    CONCAT('Migrated from legacy boolean fields: is_admin=', is_admin, ', is_super_admin=', is_super_admin, ', is_muted=', is_muted, ', is_banned=', is_banned)
FROM users
WHERE role_changed_by = 'SYSTEM_MIGRATION'
AND id NOT IN (SELECT user_id FROM role_audit_log WHERE change_type = 'SYSTEM_MIGRATION');

-- Create view for role statistics (helpful for reporting)
CREATE OR REPLACE VIEW role_statistics AS
SELECT
    user_role,
    COUNT(*) as user_count,
    COUNT(CASE WHEN last_activity_at >= DATE_SUB(NOW(), INTERVAL 1 HOUR) THEN 1 END) as active_last_hour,
    COUNT(CASE WHEN last_activity_at >= DATE_SUB(NOW(), INTERVAL 24 HOUR) THEN 1 END) as active_last_day,
    COUNT(CASE WHEN last_activity_at >= DATE_SUB(NOW(), INTERVAL 7 DAY) THEN 1 END) as active_last_week,
    AVG(message_count) as avg_message_count,
    MIN(message_count) as min_message_count,
    MAX(message_count) as max_message_count,
    AVG(DATEDIFF(NOW(), account_created_at)) as avg_account_age_days
FROM users
GROUP BY user_role
ORDER BY
    CASE user_role
        WHEN 'NEW_MEMBER' THEN 1
        WHEN 'MEMBER' THEN 2
        WHEN 'VIP' THEN 3
        WHEN 'MODERATOR' THEN 4
        WHEN 'ADMIN' THEN 5
        WHEN 'SUPER_ADMIN' THEN 6
    END;

-- Create stored procedure for automatic role progression (for future batch processing)
DELIMITER //
CREATE PROCEDURE IF NOT EXISTS CheckRoleProgression()
BEGIN
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        RESIGNAL;
    END;

    START TRANSACTION;

    -- Find New Members eligible for Member role (7+ days, 10+ messages)
    UPDATE users
    SET
        user_role = 'MEMBER',
        role_assigned_at = NOW(),
        role_changed_by = 'SYSTEM_AUTO',
        last_role_progression_check = NOW()
    WHERE
        user_role = 'NEW_MEMBER'
        AND account_created_at <= DATE_SUB(NOW(), INTERVAL 7 DAY)
        AND message_count >= 10
        AND last_role_progression_check IS NULL OR last_role_progression_check < DATE_SUB(NOW(), INTERVAL 1 HOUR);

    -- Find Members eligible for VIP role (30+ days, 100+ messages)
    UPDATE users
    SET
        user_role = 'VIP',
        role_assigned_at = NOW(),
        role_changed_by = 'SYSTEM_AUTO',
        last_role_progression_check = NOW()
    WHERE
        user_role = 'MEMBER'
        AND role_assigned_at <= DATE_SUB(NOW(), INTERVAL 30 DAY)
        AND message_count >= 100
        AND last_role_progression_check IS NULL OR last_role_progression_check < DATE_SUB(NOW(), INTERVAL 1 HOUR);

    COMMIT;
END //
DELIMITER ;

-- Update last_activity_at for recently active users (if they have existing messages)
-- This helps establish activity baseline for existing users
UPDATE users
SET last_activity_at = (
    SELECT COALESCE(MAX(timestamp), account_created_at)
    FROM messages
    WHERE messages.sender = users.username
    LIMIT 1
)
WHERE last_activity_at IS NULL
AND EXISTS (SELECT 1 FROM messages WHERE messages.sender = users.username LIMIT 1);

-- Create function to check if user can be promoted to target role
DELIMITER //
CREATE FUNCTION IF NOT EXISTS CanPromoteUser(
    current_user_id BIGINT,
    target_user_id BIGINT,
    target_role VARCHAR(20)
) RETURNS BOOLEAN
READS SQL DATA
DETERMINISTIC
BEGIN
    DECLARE current_user_level INT;
    DECLARE target_user_level INT;
    DECLARE target_role_level INT;

    -- Get role levels
    SELECT
        (SELECT u2.user_role
         FROM users u2
         WHERE u2.id = current_user_id) INTO current_user_level,
        (SELECT u3.user_role
         FROM users u3
         WHERE u3.id = target_user_id) INTO target_user_level;

    -- Convert role names to levels
    SET current_user_level = CASE current_user_level
        WHEN 'NEW_MEMBER' THEN 1
        WHEN 'MEMBER' THEN 2
        WHEN 'VIP' THEN 3
        WHEN 'MODERATOR' THEN 4
        WHEN 'ADMIN' THEN 5
        WHEN 'SUPER_ADMIN' THEN 6
        ELSE 1
    END;

    SET target_user_level = CASE target_user_level
        WHEN 'NEW_MEMBER' THEN 1
        WHEN 'MEMBER' THEN 2
        WHEN 'VIP' THEN 3
        WHEN 'MODERATOR' THEN 4
        WHEN 'ADMIN' THEN 5
        WHEN 'SUPER_ADMIN' THEN 6
        ELSE 1
    END;

    SET target_role_level = CASE target_role
        WHEN 'NEW_MEMBER' THEN 1
        WHEN 'MEMBER' THEN 2
        WHEN 'VIP' THEN 3
        WHEN 'MODERATOR' THEN 4
        WHEN 'ADMIN' THEN 5
        WHEN 'SUPER_ADMIN' THEN 6
        ELSE 1
    END;

    -- Business rules for promotion
    -- Super Admin can promote to any role except transferring ownership
    IF current_user_level = 6 AND target_role != 'SUPER_ADMIN' THEN
        RETURN TRUE;
    END IF;

    -- Admin can promote up to Moderator
    IF current_user_level = 5 AND target_role_level <= 4 THEN
        RETURN TRUE;
    END IF;

    -- Moderator can promote up to VIP
    IF current_user_level = 4 AND target_role_level <= 3 THEN
        RETURN TRUE;
    END IF;

    RETURN FALSE;
END //
DELIMITER ;

-- Add comments to document the new role system
ALTER TABLE users COMMENT = 'Extended user table with comprehensive role-based permission system. Legacy boolean fields maintained for backward compatibility.';

-- Log migration completion
INSERT INTO role_audit_log (user_id, username, previous_role, new_role, changed_by, change_type, change_reason)
VALUES (
    NULL,
    'SYSTEM',
    'MIGRATION_START',
    'MIGRATION_COMPLETE',
    'DATABASE_MIGRATION',
    'SYSTEM_MIGRATION',
    CONCAT('V2 migration completed at ', NOW(), '. Added role system with ',
           (SELECT COUNT(*) FROM users), ' users migrated.')
);

-- Show migration summary
SELECT
    'Migration Summary' as information,
    COUNT(*) as total_users,
    SUM(CASE WHEN user_role = 'SUPER_ADMIN' THEN 1 ELSE 0 END) as super_admins,
    SUM(CASE WHEN user_role = 'ADMIN' THEN 1 ELSE 0 END) as admins,
    SUM(CASE WHEN user_role = 'MODERATOR' THEN 1 ELSE 0 END) as moderators,
    SUM(CASE WHEN user_role = 'VIP' THEN 1 ELSE 0 END) as vips,
    SUM(CASE WHEN user_role = 'MEMBER' THEN 1 ELSE 0 END) as members,
    SUM(CASE WHEN user_role = 'NEW_MEMBER' THEN 1 ELSE 0 END) as new_members,
    SUM(message_count) as total_messages,
    AVG(message_count) as avg_messages_per_user
FROM users;