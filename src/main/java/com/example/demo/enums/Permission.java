package com.example.demo.enums;

public enum Permission {
    DELETE_OWN_MESSAGES,
    DELETE_ANY_MESSAGES,
    BAN_USERS,
    MUTE_USERS,
    ASSIGN_ROLES,
    CHANGE_SETTINGS,
    VIEW_ADMIN_PANEL,
    MANAGE_MUSIC,
    UPLOAD_FILES,
    DELETE_FILES,
    EDIT_PROFILE,
    SEND_MESSAGES,
    CREATE_ROOMS,
    DELETE_ROOMS,
    PIN_MESSAGES,
    UNPIN_MESSAGES,
    KICK_USERS,
    VIEW_AUDIT_LOGS,
    MANAGE_PERMISSIONS,
    SUPER_ADMIN_ACTIONS,
    SEND_LINKS,
    UPLOAD_IMAGES,
    USE_CUSTOM_EMOJIS,
    ACCESS_VIP_CHANNELS,
    DELETE_MESSAGES,
    VIEW_AUDIT_LOG,
    MANAGE_ROLES,
    ACCESS_ADMIN_PANEL,
    VIEW_SYSTEM_STATS,
    SYSTEM_CONFIGURATION,
    DATABASE_ACCESS,
    OWNERSHIP_TRANSFER;

        public PermissionCategory getCategory() {
            // TODO: Implement actual category mapping
            return PermissionCategory.GENERAL;
        }

    public static Permission fromName(String name) {
        for (Permission p : Permission.values()) {
            if (p.name().equalsIgnoreCase(name)) {
                return p;
            }
        }
        throw new IllegalArgumentException("No permission with name: " + name);
    }

    public String getName() {
        return name();
    }
}

