package com.example.demo.service;

import org.springframework.stereotype.Service;

import com.example.demo.model.User;
import com.example.demo.model.UserRole;

@Service
public class PermissionService {
    public boolean canSendLink(User user) {
        // Only MEMBER and above can send links
        return user.getUserRole() != null && user.getUserRole().ordinal() >= UserRole.MEMBER.ordinal();
    }

    public boolean canDeleteMessage(User user) {
        // Only MODERATOR and above can delete messages
        return user.getUserRole() != null && user.getUserRole().ordinal() >= UserRole.MODERATOR.ordinal();
    }

    public boolean canBanUser(User user) {
        // Only ADMIN and above can ban users
        return user.getUserRole() != null && user.getUserRole().ordinal() >= UserRole.ADMIN.ordinal();
    }

    public boolean canAccessAdminPanel(User user) {
        // Only ADMIN and SUPER_ADMIN can access admin panel
        return user.getUserRole() == UserRole.ADMIN || user.getUserRole() == UserRole.SUPER_ADMIN;
    }

    public boolean canAccessModeratorTools(User user) {
        // Only MODERATOR and above
        return user.getUserRole() != null && user.getUserRole().ordinal() >= UserRole.MODERATOR.ordinal();
    }
}
