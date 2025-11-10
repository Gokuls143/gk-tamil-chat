package com.example.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.model.UserRole;
import com.example.demo.service.RoleService;

@RestController
@RequestMapping("/api/roles")
public class RoleController {
    @Autowired
    private RoleService roleService;

    @PostMapping("/assign")
    public ResponseEntity<?> assignRole(@RequestParam Long userId,
                                        @RequestParam UserRole newRole,
                                        @RequestParam(required = false) String auditNote) {
        boolean success = this.roleService.assignRole(userId, newRole, auditNote);
        if (success) {
            return ResponseEntity.ok("Role assigned successfully.");
        } else {
            return ResponseEntity.badRequest().body("User not found.");
        }
    }

    @GetMapping("/get")
    public ResponseEntity<?> getUserRole(@RequestParam Long userId) {
        UserRole role = this.roleService.getUserRole(userId);
        if (role != null) {
            return ResponseEntity.ok(role);
        } else {
            return ResponseEntity.badRequest().body("User not found.");
        }
    }
}
